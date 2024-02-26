/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hardwareLayer.processors;

import configuration.Configuration;
import configuration.data.HardwareElementIdentifier;
import hardwareLayer.HardwareDataProcessor;
import static hardwareLayer.HardwareDataProcessor.STRING_ACK;
import static hardwareLayer.HardwareDataProcessor.STRING_NAK;
import hardwareLayer.writers.AbstractWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/**
 *
 * @author TEXTEL
 */
public class MessageProcessorForLG extends AbstractMessageProcessor{

    private static MessageProcessorForLG instance = null;
    private final Logger LOG = Logger.getLogger(MessageProcessorForLG.class.getName());
    private final String PROTOCOL = HardwareDataProcessor.LG_PROTOCOL;
    public static final int BAUDRATE_FOR_LG = SerialPort.BAUDRATE_9600;
    
    private final String LG_OK = "OK";
    private final String LG_KO = "NG";


    private MessageProcessorForLG(){}

    public static AbstractMessageProcessor getInstance(){
        if(instance == null){
            instance = new MessageProcessorForLG();
        }
        
        return instance;
    }

    @Override
    public String tryToRetrieveCardId(String portName) throws SerialPortException{
        String cardId = "";

        // Abrimos y configuramos el puerto serie
        SerialPort serialPortAux = new SerialPort(portName);
        serialPortAux.openPort();
        serialPortAux.setParams(
                BAUDRATE_FOR_LG,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPortAux.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        
        //Obtenemos los posibles IDs
        ArrayList<String> ids = Configuration.getInstance().getCardIdsByProtocol(PROTOCOL);
        
        // Obtenemos el menzaje
        String baseMessage = Configuration.getInstance().getQueryForCardIdByProtocol(PROTOCOL);

        if(baseMessage != null && !baseMessage.isEmpty()){
            
            for(int index = 0; index < ids.size() && cardId.isEmpty(); index++){
                String currentID = ids.get(index);
                String message = Configuration.CR + baseMessage.replace("ID", currentID) + Configuration.CR;////// solucion encendido
                serialPortAux.writeBytes(message.getBytes());

                // Preparo mi buffer (variable 'name')
                String name = new String();
                boolean end = false;

                // Leemos la respuesta de byte en byte hasta que hayamos leido un ETX
                while (!end) {

                    try {
                        byte[] tmpBuffer = serialPortAux.readBytes(1, 1000);
                        char data = (char) tmpBuffer[0]; // Pasamos del valor ascii a su representacion
                        
                        /* Recibimos 'x' */
                        if (data == 'x') {   // LG's end of package
                            name = name.trim();
                            end = true;

                        /* Recibimos datos */
                        } else {
                            name += String.valueOf(data);
                        }

                    } catch (SerialPortTimeoutException ex) {
                        name = "";
                        end = true;
                    }
                }

                /* Procesamos la respuesta */
                if(!name.isEmpty() && name.length() == 9){
                    cardId = name.substring(2, 4);
                    
                    if(!cardId.equals(currentID)){
                        LOG.warn("PortName [" + portName + "] answered an unexpected id [" +
                                cardId + "] since we expect [" + currentID + "]");
                    }
                }else if(!name.isEmpty()){
                    LOG.error("Unexpected initial answer [" + name + "]");
                }
            }
            
        }else{
            LOG.error("There is no query for cardId by protocol " + PROTOCOL + ". Please, check QUERY_FOR_CARD_ID attribute on CARD tags.");
        }

        // Cierro el puerto
        serialPortAux.closePort();
        
        return cardId;
    }


    @Override
    public boolean sendCommand(String commandID, String cardID, boolean isClosing, AbstractWriter oldWriter) {
        boolean success = false;

        // Primero hay que parar al escritor
        SerialPort serialPort = oldWriter.getSerialPort();

        if (serialPort != null) {
            try {
                oldWriter.setCanWrite(false);

                // Comunicacion con la tarjeta
                String message = commandID.replace("ID", cardID) + Configuration.CR;
                serialPort.writeBytes(message.getBytes());

                // reestablecer comuniciacion anterior
                oldWriter.setCanWrite(true);
                success = true;

            } catch (SerialPortException ex) {
                LOG.error(ex);
            }

        } else {
            LOG.error("No se ha podido enviar el comando " + commandID + " a "
                    + cardID + " porque se ha perdido la conexion con el puerto serie.");
        }

        return success;
    }


    @Override
    void processCommand(String newMessage, SnmpAgent snmpAgent, String faceId, String cardId) {
        String format = Configuration.getInstance().getCommandResponseFormat(faceId, cardId, newMessage.substring(0, 1));
        this.processQueryMessage(format, newMessage, faceId, cardId, snmpAgent);
    }


    @Override
    void processNewValue(String faceId, String cardId, String idOfQueryResponse, String newValue, boolean valueIsNumber, SnmpAgent snmpAgent) {
        boolean isCommand = this.isCommandResponse(idOfQueryResponse, faceId, cardId);
        
        if(newValue.length() >= 6){
            String setId = newValue.substring(0, 2);
            String state = newValue.substring(2, 4);
            String data = newValue.substring(4);
            
            if(cardId.equals(setId)){
                
                /* Dado que para LG la respuesta del comando y query son iguales, ahora es
                    cuando realmente hay que ver si ademas es un comando */
                if(isCommand){
                    this.updateAnswerCommand(idOfQueryResponse, newValue, snmpAgent);
                }

                switch (state) {
                    case LG_OK:
                        HardwareElementIdentifier id = new HardwareElementIdentifier(faceId, cardId, idOfQueryResponse);
                        String oid = Configuration.getInstance().getOidForQueries(id);

                        if (oid != null) {
                            snmpAgent.updateInfo(oid, data);

                        } else {
                            if(!isCommand){
                                LOG.error("No se ha encontrado un oid para " + id);
                            }
                        }
                        break;

                    case LG_KO:
                        LOG.info("KO recicibed for query response ID [" + idOfQueryResponse + "]. Info: faceId ["
                                + faceId + "] cardId [" + cardId + "]");
                        break;

                    default:
                        LOG.error("Unexpected state [" + state + "] for query response ID [" + idOfQueryResponse + "]");
                }

            }else{
                LOG.error("Unexpected id [" + setId + "] on response for query response ID [" + idOfQueryResponse +
                        "]. It was exptected to be equals to [" + cardId + "]");
            }

        }else{
            LOG.error("Unexpected value [" + newValue + "] for query response ID [" + idOfQueryResponse + "]");
        }
    }

    @Override
    boolean isCommandResponse(String newMessage, String faceId, String cardId) {
        String format = Configuration.getInstance().getCommandResponseFormat(faceId, cardId, newMessage.substring(0, 1));
        return (format != null);
    }

    private void updateAnswerCommand(String commandId, String newValue, SnmpAgent snmpAgent) {
        String setId = newValue.substring(0, 2);
        String state = newValue.substring(2, 4);
        String data = newValue.substring(4);
        
        
        if (!state.isEmpty()) {
            String command = commandId + " ID";

            if (state.equals(LG_OK)) {
                //LOG.debug("La respuesta del comando " + commandId + " es: " + STRING_ACK);
                snmpAgent.updateAnswer(command, STRING_ACK);

            } else if (state.equals(LG_KO)) {
                //LOG.debug("La respuesta del comando " + commandId + " es: " + STRING_NAK);
                snmpAgent.updateAnswer(command, STRING_NAK);

            } else {
                LOG.debug("Respuesta inesperada del comando " + commandId + ": " + state);
            }

        } else {
            LOG.debug("Command response " + commandId + " without state");
        }
    }
    
    
    /* Para LG hay que quitar los  datos del comando, por lo que nos quedamos con los 5 primeros caracteres */
    @Override
    public String commandToAnswerCommand(String command) {
        return command.substring(1, 5);
    }
    
 }
