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
import static hardwareLayer.HardwareDataProcessor.WRONG_LEDS;
import static hardwareLayer.HardwareDataProcessor.WRONG_MODULES;
import hardwareLayer.writers.AbstractWriter;
import java.util.TreeSet;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/**
 *
 * @author TEXTEL
 */
public class MessageProcessorForTextel extends AbstractMessageProcessor{

    private static MessageProcessorForTextel instance = null;
    private static final Logger LOG = Logger.getLogger(MessageProcessorForTextel.class.getName());
    private static final String PROTOCOL = HardwareDataProcessor.TEXTEL_PROTOCOL;
    public static final int BAUDRATE_FOR_TEXTEL = SerialPort.BAUDRATE_57600;


    private MessageProcessorForTextel(){}

    public static AbstractMessageProcessor getInstance(){
        if(instance == null){
            instance = new MessageProcessorForTextel();
        }
        
        return instance;
    }


    @Override
    void processNewValue(String faceId, String cardId, String idOfQueryResponse, String newValue, boolean valueIsNumber, SnmpAgent snmpAgent) {
        boolean isValueOk = true;

        if (valueIsNumber) {
            try {
                Integer.parseInt(newValue);

            } catch (NumberFormatException ex) {
                isValueOk = false;
                LOG.error("Se descarta el valor " + newValue + " del id " + idOfQueryResponse + " por no ser numerico");
            }
        }

        //LOG.info("Procesando con idOfQueryResponse: " + idOfQueryResponse);
        
        if (idOfQueryResponse.equals("E")) {

            TreeSet<Integer> wrongModuleRows = new TreeSet<>();
            int numberOfWrongLeds = 0;
            int numberOfWrongModules = 0;

            int moduleWidth = Configuration.getInstance().getModuleWidth();
            int moduleHeight = Configuration.getInstance().getModuleHeight();
            int widthInModules = Configuration.getInstance().getWidthInModules();

            char[] leds = newValue.toCharArray();
            
            StringBuffer hex = new StringBuffer();
            for (int i = 0; i < leds.length; i++)
            {
                hex.append(Integer.toHexString((int) leds[i]));
                hex.append(new String(new byte[] { 0x20}));
            }
            System.out.println("ANGEL");
            System.out.println(hex.toString());    

            for (int i = 0; i < leds.length; i++) {

                // averiguamos el valor ASCII que contiene el char
                int currentState = ((int) leds[i]) & 0xff;

                // si los 4 primeros bits son 0, está mal un cable o un integrado
                // va a suceder lo mismo a todos los leds de ese módulo
                if (currentState < 16) {
                    numberOfWrongModules++;
                    int row = i / (moduleWidth * widthInModules);
                    wrongModuleRows.add((row / moduleHeight) + 1);

                    // si los otros cuatro bits también están a uno, todo está bien
                    // si no, falla algún color del led
                } else if (currentState != 255) {
                    numberOfWrongLeds++;
                    int row = (i / (moduleWidth * widthInModules)) + 1;
                    int column = (i % (moduleWidth * widthInModules)) + 1;
                    LOG.info("Faulty led found at row [" + row + "] column [" + column + "].");
                }
            }

            for (int currentModuleWrongAtRow : wrongModuleRows) {
                LOG.info("Faulty module (or cabling) found at row [" + currentModuleWrongAtRow + "].");
            }

            // el número de wrongModules es ahora mismo un contador de leds
            // hay que dividir entre el número de filas, columnas y placas por fila
            // para traducir el contador de leds a contador de placas
            numberOfWrongModules = numberOfWrongModules / (moduleWidth * moduleHeight * widthInModules);

            /* Actualizamos el numero de leds errorneos */
            HardwareElementIdentifier idForWrongLeds = new HardwareElementIdentifier(faceId, cardId, WRONG_LEDS);
            String oidForWrongLeds = Configuration.getInstance().getOidForQueries(idForWrongLeds);

            if (oidForWrongLeds != null) {
                snmpAgent.updateInfo(oidForWrongLeds, String.valueOf(numberOfWrongLeds));
            } else {
                LOG.error("No se ha encontrado un oid para " + idForWrongLeds);
            }

            /* Actualizamos el numero de modulos errorneos */
            HardwareElementIdentifier idForWrongModules = new HardwareElementIdentifier(faceId, cardId, WRONG_MODULES);
            String oidForWrongModules = Configuration.getInstance().getOidForQueries(idForWrongModules);

            if (oidForWrongModules != null) {
                snmpAgent.updateInfo(oidForWrongModules, String.valueOf(numberOfWrongModules));
            } else {
                LOG.error("No se ha encontrado un oid para " + idForWrongModules);
            }

        } else {
            if (isValueOk) {
                HardwareElementIdentifier id = new HardwareElementIdentifier(faceId, cardId, idOfQueryResponse);
                String oid = Configuration.getInstance().getOidForQueries(id);

                if (oid != null) {
                    snmpAgent.updateInfo(oid, newValue);
                } else {
                    LOG.error("No se ha encontrado un oid para " + id);
                }
            }
        }
    }
    
    @Override
    public void processCommand(String newMessage, SnmpAgent snmpAgent, String faceId, String cardId) {
        if (newMessage.length() > 3) {
            String command = newMessage.substring(1, 3);
            String answer = newMessage.substring(3, 4);

            if (answer.charAt(0) == Configuration.ACK) {
                //LOG.debug("La respuesta del comando " + command + " es: " + STRING_ACK);
                snmpAgent.updateAnswer(command, STRING_ACK);

            } else if (answer.charAt(0) == Configuration.NAK) {
                //LOG.debug("La respuesta del comando " + command + " es: " + STRING_NAK);
                snmpAgent.updateAnswer(command, STRING_NAK);

            } else {
                snmpAgent.updateAnswer(command, newMessage.substring(3));
                LOG.info("La respuesta para el comando " + command + " no es ack/nak, se ha recibido: " + newMessage.substring(3));
            }

        } else {
            LOG.debug("Unexpected command message: " + newMessage);
        }
    }

    @Override
    public String tryToRetrieveCardId(String portName) throws SerialPortException{
        String cardId = "";

        // Abrimos y configuramos el puerto serie
        SerialPort serialPortAux = new SerialPort(portName);
        serialPortAux.openPort();
        serialPortAux.setParams(
                BAUDRATE_FOR_TEXTEL,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // Mandamos la pregun Q
        String message = Configuration.getInstance().getQueryForCardIdByProtocol(PROTOCOL);
        
        if(message != null){
        
            message = Configuration.STX + message + Configuration.ETX;
            serialPortAux.writeBytes(message.getBytes());

            // Preparo mi buffer (variable 'name')
            String name = new String();
            boolean start = false;
            boolean end = false;

            // Leemos la respuesta de byte en byte hasta que hayamos leido un ETX
            while (!end) {

                try {
                    byte[] tmpBuffer = serialPortAux.readBytes(1, 1000);
                    char data = (char) tmpBuffer[0]; // Pasamos del valor ascii a su representacion
                    
                    if (data == Configuration.STX) {
                        start = true;
                        
                    } else if (data == Configuration.ETX) {
                        end = true;
                        
                    } else {
                        name += String.valueOf(data);
                    }

                } catch (SerialPortTimeoutException ex) {
                    name = "";
                    end = true;
                }
            }

            // Cierro el puerto
            serialPortAux.closePort();

            // Si el mensaje está completo
            if (start && end) {
                cardId = name;
            }

        }else{
            LOG.error("There is no query for cardId by protocol " + PROTOCOL + ". Please, check QUERY_FOR_CARD_ID attribute on CARD tags.");
        }

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
                String message = Configuration.STX + commandID + Configuration.ETX;
                System.out.println("Command: " + message.toString());
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
    public boolean isCommandResponse(String newMessage, String faceId, String cardId) {
        boolean isCommandResponse = false;

        if (newMessage.substring(0, 1).equals(ANSWER_COMMAND_ID)) {
            isCommandResponse = true;
        }
        
        return isCommandResponse;
    }

    
    /* Para textel el comando es el mismo que la respuesta */
    @Override
    public String commandToAnswerCommand(String command) {
        return command;
    }
}
