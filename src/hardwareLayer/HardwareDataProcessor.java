package hardwareLayer;

import hardwareLayer.readers.SerialPortReaderForTextel;
import configuration.Configuration;
import configuration.data.ScheduledQueryList;
import hardwareLayer.processors.AbstractMessageProcessor;
import hardwareLayer.processors.MessageProcessorForLG;
import hardwareLayer.processors.MessageProcessorForTextel;
import hardwareLayer.readers.AbstractReader;
import hardwareLayer.readers.SerialPortReaderForLG;
import hardwareLayer.writers.AbstractWriter;
import hardwareLayer.writers.SerialPortWriterForLG;
import hardwareLayer.writers.SerialPortWriterForTextel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Tecnovision
 */
public class HardwareDataProcessor extends Thread {

    
    public final static String TEXTEL_PROTOCOL = "TEXTEL";
    public final static String LG_PROTOCOL = "LG";

    private final static int MAX_MESSAGES = 10;
    public final static String STRING_ACK = "ACK";
    public final static String STRING_NAK = "NAK";
    public final static String STRING_NO_VALUE = "NO_VALUE";

    public final static String WRONG_LEDS = "wrongLeds";
    public final static String WRONG_MODULES = "wrongModules";


    private Timer timer;
    private boolean exit;
    private final String faceId;
    private final TreeSet<String> cardIDs;                              // IDs de las tarjetas fisicas
    private final SnmpAgent snmpAgent;
    private final HashMap<String, AbstractWriter> cardIdToWriter;     // Writers de los puertos
    private final HashMap<String, AbstractReader> cardIdToReader;     // Listeners de los puertos
    private HashMap<String, String> cardIdToPortName;                   // Para configurar el puerto serie
    private final Object mutexForCardIdToPortName;
    private final ArrayList<SerialPortMessage> messagesFromReaders;
    private final Object mutexFroMessagesFromReaders;

    private static final Logger LOG = Logger.getLogger(HardwareDataProcessor.class.getName());

    public HardwareDataProcessor(String face, TreeSet<String> cards, SnmpAgent agent) {
        cardIdToReader = new HashMap<>();
        cardIdToWriter = new HashMap<>();
        cardIdToPortName = new HashMap<>();
        mutexForCardIdToPortName = new Object();
        messagesFromReaders = new ArrayList<>();
        mutexFroMessagesFromReaders = new Object();
        snmpAgent = agent;
        cardIDs = cards;
        faceId = face;
        exit = false;

        // buscamos, abrimos y configuramos los puertos
        // de paso, creamos a los lectores
        this.initSerialPorts();

        // Creamos el escritor de todas las tarjetas
        for (Iterator<String> cardIt = cardIDs.iterator(); cardIt.hasNext();) {
            timer = new Timer(true);
            String currentCardId = cardIt.next();
            SerialPort serialPortForCurrentCardId = cardIdToReader.get(currentCardId).getSerialPort();
            String protocol = Configuration.getInstance().getProtocolByCardId(currentCardId);
            
            if(protocol != null){
                AbstractWriter currentWriter = null;

                switch(protocol){////// intentar reducir el numero de switch!!
                    case TEXTEL_PROTOCOL:
                        currentWriter = new SerialPortWriterForTextel(serialPortForCurrentCardId, this, currentCardId, faceId, snmpAgent);
                        break;
                        
                    case LG_PROTOCOL:
                        currentWriter = new SerialPortWriterForLG(serialPortForCurrentCardId, this, currentCardId, faceId, snmpAgent);
                        break;

                    default:
                        LOG.error("Unexpected protocol [" + protocol + "] for cardId [" + currentCardId + "]. Please check attributes of CARD tags.");
                        System.exit(-1);
                }

                cardIdToWriter.put(currentCardId, currentWriter);
                timer.scheduleAtFixedRate(currentWriter, 0, 1000 * 1);

                // Asiganamos todas las queries al escritor
                ArrayList<ScheduledQueryList> queries = Configuration.getInstance().getScheduledQueries(faceId, currentCardId);
                currentWriter.setScheduledQueries(queries);

            }else{
                LOG.error("There is no protocol for cardId [" + currentCardId + "]. Please check attributes of CARD tags.");
            }
        }
    }

    // buscamos, abrimos y configuramos los puertos
    // de paso, creamos a los lectores
    private void initSerialPorts() {

        // Declaramos un Hashmap que tendrá como clave el nombre de la placa (AL1, AL2, ER1, ER2)
        // y como valor un String con el nombre del puerto
        synchronized (mutexForCardIdToPortName) {
            cardIdToPortName = new HashMap<>();
            mutexForCardIdToPortName.notifyAll();
        }

        // Obtenemos los nombres de los puertos disponibles
        // buscamos qué puertos serie tienen alguna tarjeta conectada
        for (String serialPortName : retrieveSerialPortNames()) {
            try {
                lookingForAllCardIDs(serialPortName);
            } catch (SerialPortTimeoutException | SerialPortException ex) {
                LOG.debug("Desestimado el puerto " + serialPortName + " al no recibir respuesta");
            }
        }

        // Inicializamos nuestros puertos series
        for (String currentCardId : cardIDs) {
            String currentPortName = this.getCardIdToPortNameValue(currentCardId);
            SerialPort currentSerialPort = this.configurePort(currentPortName, currentCardId);

            // Creamos y asignamos los Listener
            try {
                
                String protocol = Configuration.getInstance().getProtocolByCardId(currentCardId);
                protocol = (protocol != null) ? protocol : "";
                
                if(!protocol.isEmpty()){
                    AbstractReader reader = null;
                    
                    switch(protocol){
                        case TEXTEL_PROTOCOL:
                            reader = new SerialPortReaderForTextel(currentSerialPort, faceId, currentCardId, this);
                            break;
                            
                        case LG_PROTOCOL:
                            reader = new SerialPortReaderForLG(currentSerialPort, faceId, currentCardId, this);
                            break;

                        default:
                            LOG.error("Unexpected protocol [" + protocol + "] for cardID [" + currentCardId + "]");
                            System.exit(-1);
                    }
                    
                    
                    cardIdToReader.put(currentCardId, reader);

                    if (currentSerialPort != null) {
                        currentSerialPort.addEventListener(cardIdToReader.get(currentCardId));
                    }

                }else{
                    LOG.error("Unexpected or empty protocol [" + protocol + "] for cardId [" + currentCardId + "]. Please check PROTOCOL attribute of CARD tags");
                }

            } catch (SerialPortException ex) {
                LOG.error("ERROR al asignar el reader");
            }
        }
    }

    private void lookingForAllCardIDs(String portName) throws SerialPortException, SerialPortTimeoutException {

        String cardId = "";
        LinkedHashSet<String> protocols = Configuration.getInstance().getProtocols();

        for(String currentProtocol : protocols){
        
            switch(currentProtocol){
                case TEXTEL_PROTOCOL:
                    cardId = MessageProcessorForTextel.getInstance().tryToRetrieveCardId(portName);
                    break;
                    
                case LG_PROTOCOL:
                    cardId = MessageProcessorForLG.getInstance().tryToRetrieveCardId(portName);
                    break;

                default:
                    LOG.error("Unexpected protocol: " + currentProtocol);
            }
            
            // Si ya tenemos el Id no seguimos buscando!
            if(!cardId.isEmpty()){ ///// mirar si se puede meter la condicion al for
                break;
            }
        }

        // Si tenemos ID
        if (!cardId.isEmpty()){
            // Alimento el Hashmap con un nuevo registro
            this.setCardIdToPortNameValue(cardId, portName);
            
        // Para emular el funcionamiento de textel como si no tuviesemos respuesta
        }else{
            throw new SerialPortTimeoutException(portName, "tryToRetrieveCardId", 1000);
        }
    }

    private SerialPort configurePort(String portName, String cardID) {
        SerialPort serialPort = null;

        if (portName != null) {
            
            String protocol = Configuration.getInstance().getProtocolByCardId(cardID);
            
            if(protocol != null && !protocol.isEmpty()){

                int speed;

                switch(protocol){
                    case TEXTEL_PROTOCOL:
                        speed = MessageProcessorForTextel.BAUDRATE_FOR_TEXTEL;
                        break;

                    case LG_PROTOCOL:
                        speed = MessageProcessorForLG.BAUDRATE_FOR_LG;
                        break;

                    default:
                        speed = SerialPort.BAUDRATE_57600;
                        LOG.error("Unexpected protocol [" + protocol + "] for cardID [" + cardID + "]. Assuming BAUDRATE equals to 57600");
                }

                try {
                    serialPort = new SerialPort(portName);
                    serialPort.openPort();
                    serialPort.setParams(
                            speed,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    int mask = SerialPort.MASK_RXCHAR;   //Prepare mask
                    serialPort.setEventsMask(mask);   //Set mask
                    LOG.info("+++ Detectada la tarjeta " + cardID + " en el puerto " + portName);

                } catch (SerialPortException ex) {
                    serialPort = null;
                    LOG.info("--- No se ha encontrado la tarjeta " + cardID);
                }
            
            }else{
                LOG.error("Unexpected or empty protocol [" + protocol + "] for cardId [" + cardID + "]. Please check PROTOCOL attribute of CARD tags");
                serialPort = null;
            }

        } else {
            serialPort = null;
            LOG.error("---------- No se ha encontrado la tarjeta " + cardID);

            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException ex) {
                LOG.error(ex);
            }
        }

        return serialPort;
    }

    public SerialPort reconfigureSerialPort(SerialPort port, String cardId) {
        // Declaramos un Hashmap que tendrá como clave el nombre de la placa (AL1, AL2, ER1, ER2)
        // y como valor un String con el nombre del puerto
        this.removeCardIdToPortNameValue(cardId);
        
        /* Limpiamos la info del agente de la tarjeta que se ha perdido la conexion */
        ArrayList<String> oidList = Configuration.getInstance().getOidList(faceId, cardId);
        oidList = (oidList != null) ? oidList : new ArrayList<String>();
        
        for (String currentOid : oidList) {
            snmpAgent.updateInfo(currentOid, "");
        }

        try {

            // Obtenemos los nombres de los puertos disponibles
            // buscamos las dos tarjetas por todos los puertos serie disponibles
            for (String serialPortName : retrieveSerialPortNames()) {
                try {
                    lookingForAllCardIDs(serialPortName);
                } catch (SerialPortTimeoutException | SerialPortException ex) {
                    //log.debug("Desestimado el puerto " + serialPortName + " al no recibir respuesta");
                }
            }

        } catch (NullPointerException e) {
            LOG.error("Error al pedir al SO la lista de puertos serie.");
        }

        // Inicializamos nuestros puertos series
        String alarmsPortName = this.getCardIdToPortNameValue(cardId);
        port = this.configurePort(alarmsPortName, cardId);

        // Creamos y asignamos los Listener
        try {

            if (port != null) {
                AbstractReader reader = cardIdToReader.get(cardId);
                reader.updateSerialPort(port);
                port.addEventListener(reader);
            }

        } catch (SerialPortException ex) {
            LOG.error("ERROR al asignar el reader: " + ex);
        }
        
        return port;
    }

    private void closeSerialPorts() {
        // finalmente, cerramos los puertos porque, si no, no finaliza el programa. Curioso!!
        try {

            for (Iterator<AbstractWriter> it = cardIdToWriter.values().iterator(); it.hasNext();) {
                it.next().closeSerialPort();
            }

        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    public void exit() {
        synchronized (mutexFroMessagesFromReaders) {
            exit = true;
            mutexFroMessagesFromReaders.notifyAll();
        }
    }

    @Override
    public void run() {

        while (!exit) {
            try {

                SerialPortMessage currentMessage = this.dequeueSerialPortMessage();
                //log.info("desencolando: " + currentMessage.getMessage());
                
                if (currentMessage != null) {
                    ////log.info("Mensaje de " + currentMessage.getCardId() + " contenido [" + currentMessage.getMessage() + "]");
                    
                    String protocol = currentMessage.getProtocol();
                    
                    switch(protocol){
                        case TEXTEL_PROTOCOL:
                            MessageProcessorForTextel.getInstance().processAbstractMessage(currentMessage.getMessage(), faceId, currentMessage.getCardId(), snmpAgent);
                            break;
                            
                        case LG_PROTOCOL:
                            MessageProcessorForLG.getInstance().processAbstractMessage(currentMessage.getMessage(), faceId, currentMessage.getCardId(), snmpAgent);
                            break;

                        default:
                            LOG.error("Unexpected protocol: " + protocol);
                    }
                }

            } catch (InterruptedException ex) {
                LOG.error("ERROR procesando los mensajes: " + ex);
            }
        }

        timer.cancel();
        this.closeSerialPorts();
    }

    public boolean sendCommand(String commandID, String cardID, boolean isClosing) {
        boolean success = false;

        // Se compruba que tengamos conexion con cardID
        if (cardIDs.contains(cardID)) {

            // Primero hay que parar al escritor
            AbstractWriter oldWriter = cardIdToWriter.get(cardID);
            String protocol = Configuration.getInstance().getProtocolByCardId(cardID);
            protocol = (protocol != null) ? protocol : "";
            
            switch(protocol){
                case TEXTEL_PROTOCOL:
                    success = MessageProcessorForTextel.getInstance().sendCommand(commandID, cardID, isClosing, oldWriter);
                    break;
                    
                case LG_PROTOCOL:
                    success = MessageProcessorForLG.getInstance().sendCommand(commandID, cardID, isClosing, oldWriter);
                    break;

                default: 
                    LOG.error("Unexpected or empty protocol [" + protocol + "] for cardId [" + cardID + "]. Please check PROTOCOL attribute of CARD tags");
            }

        } else {
            if (!isClosing) {
                LOG.warn("Unexpected card: " + cardID);
            }
        }

        return success;
    }

    public String getCardIdToPortNameValue(String key) {
        String value;
        synchronized (mutexForCardIdToPortName) {
            value = cardIdToPortName.get(key);
            mutexForCardIdToPortName.notifyAll();
        }
        return value;
    }

    public void removeCardIdToPortNameValue(String key) {
        synchronized (mutexForCardIdToPortName) {
            cardIdToPortName.remove(key);
            mutexForCardIdToPortName.notifyAll();
        }
    }

    public void setCardIdToPortNameValue(String cardName, String portName) {
        synchronized (mutexForCardIdToPortName) {
            String oldPortName = cardIdToPortName.get(cardName);

            if ((oldPortName == null) || !(portName.equals(oldPortName))) {
                cardIdToPortName.put(cardName, portName);
                LOG.info(cardName + " at " + portName);
            }
            mutexForCardIdToPortName.notifyAll();
        }
    }

    public void enqueueSerialPortMessage(SerialPortMessage message) throws InterruptedException {
        synchronized (mutexFroMessagesFromReaders) {
            if (messagesFromReaders.size() >= MAX_MESSAGES) {
                LOG.info(faceId + ":" + message.getCardId() + " Cuidado, hay " + messagesFromReaders.size()
                        + " mensajes de estado sin procesar.");
            }

            messagesFromReaders.add(message);
            mutexFroMessagesFromReaders.notifyAll();
        }
    }

    public SerialPortMessage dequeueSerialPortMessage() throws InterruptedException {
        synchronized (mutexFroMessagesFromReaders) {
            SerialPortMessage message = null;

            while ((messagesFromReaders.isEmpty()) && (!exit)) {
                try {
                    mutexFroMessagesFromReaders.wait(100);
                } catch (InterruptedException ex) {
                    LOG.error(ex);
                }
            }

            if (!messagesFromReaders.isEmpty()) {
                message = messagesFromReaders.remove(0);
                mutexFroMessagesFromReaders.notifyAll();
            }

            return message;
        }
    }

    public HashMap<String, AbstractWriter> getWriters() {
        return cardIdToWriter;
    }

    public void processorIsInitialized() {
        for (AbstractWriter currentWriter : cardIdToWriter.values()) {
            currentWriter.processorIsInitialized();
        }
    }
    
    
    private AbstractMessageProcessor getProcessor(String protocol, String cardID) {
        AbstractMessageProcessor processor = null;

        switch (protocol) {
            case TEXTEL_PROTOCOL:
                processor = MessageProcessorForTextel.getInstance();
                break;

            case LG_PROTOCOL:
                processor = MessageProcessorForLG.getInstance();
                break;

            default:
                LOG.error("Unexpected or empty protocol [" + protocol + "] for cardId [" + cardID + "]. Please check PROTOCOL attribute of CARD tags");
        }

        return processor;
    }
    
    public String commandToAnswerCommand(String command, String cardID){
        String protocol = Configuration.getInstance().getProtocolByCardId(cardID);
        protocol = (protocol != null) ? protocol : "";
        return this.getProcessor(protocol, cardID).commandToAnswerCommand(command);
    }

    private Collection<String> retrieveSerialPortNames() {
        Collection<String> ignoredSerialPortNames = Configuration.getInstance().getIgnoredSerialPortNames();

        return Arrays.stream(SerialPortList.getPortNames())
                .filter(portName -> ignoredSerialPortNames.stream().noneMatch(ignoredName -> portName.contains(ignoredName)))
                .collect(Collectors.toList());
    }
}
