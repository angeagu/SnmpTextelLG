/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hardwareLayer.processors;

import configuration.Configuration;
import configuration.data.HardwareElementIdentifier;
import configuration.data.HardwareNotificationData;
import hardwareLayer.writers.AbstractWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.logging.Level;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/**
 *
 * @author TEXTEL
 */
public abstract class AbstractMessageProcessor {///// intentar poner en el padre la parte singleton
    
    private final Logger LOG = Logger.getLogger(AbstractMessageProcessor.class.getName());

    public final static char SYMBOL = '.';
    public final static char EXCLAMATION_MARK = '!';
    public final static char NUMERICAL_CHAR = '#';
    public final static char POSITIVE_SIGNED_CHAR = '+';
    public final static char NEGATIVE_SIGNED_CHAR = '-';
    public final static char ALPHABETICAL_CHAR = '*';
    public final static char BLANK_CHAR = '_';
    public final static char NUMERICAL_STREAM = '@';
    
    public final static String FORMAT_RESPONSE_DELIMETERS = "#.*_";
    
    public final static String ANSWER_COMMAND_ID = "X";


    public abstract String tryToRetrieveCardId(String portName) throws SerialPortException;

    public abstract boolean sendCommand(String commandID, String cardID, boolean isClosing, AbstractWriter oldWriter);

    /* Le pasamos el faceId y el cardId principalmente para el protocolo LG, ya qye textel no lo necesita */
    /* El problema con el protocolo LG es que la respuesta de un commnad es exactamente igual al del query */
    abstract void processCommand(String newMessage, SnmpAgent snmpAgent, String faceId, String cardId);
    
    abstract void processNewValue(String faceId, String cardId, String idOfQueryResponse, String newValue, boolean valueIsNumber, SnmpAgent snmpAgent);
    
    abstract boolean isCommandResponse(String newMessage, String faceId, String cardId);
    
    public abstract String commandToAnswerCommand(String command);
    
    public void processAbstractMessage(String newMessage, String faceId, String cardId, SnmpAgent snmpAgent) {
        boolean isAnswerCommand = false;
        boolean isNotification = false;

        /* Obtenemos el formato de mensaje si es query */
        String answerQueryformat = Configuration.getInstance().getQueryResponseFormat(faceId, cardId, newMessage.substring(0, 1));

        /* Obtenemos el formato de mensaje si es notificacion */
        HardwareNotificationData notificationData = Configuration.getInstance().getHwNotificationData(faceId, cardId, newMessage.substring(0, 1));

        /* Miramos si es una respuesta de ejecucion de comando */
        if (this.isCommandResponse(newMessage, faceId, cardId)) {
            this.processCommand(newMessage, snmpAgent, faceId, cardId);
            isAnswerCommand = true;

            // Primero miramos si es una notificacion hardware
        } else if (notificationData != null && notificationData.getNotificationFormat().length() == newMessage.length()) {
            String id = "", value = "";
            boolean error = false;
            boolean valueIsNumber = false;
            String notificationFormat = notificationData.getNotificationFormat();
            
            // Recorremos posicion a posicion
            for (int index = 0; index < newMessage.length(); index++) {

                // Si es SYMBOL tambien pertenece al valor, si éste no es numérico
                if (notificationFormat.charAt(index) == SYMBOL) {

                    // Comprobamos que sea un simbolo contemplado
                    if (newMessage.charAt(index) == EXCLAMATION_MARK) {
                        if(!valueIsNumber) {
                            value += String.valueOf(newMessage.charAt(index));
                        }

                    // Error en el simbolo
                    } else {
                        error = true;
                    }

                } else if(notificationFormat.charAt(index) == ALPHABETICAL_CHAR) {
                    
                    if(Character.isLetter(newMessage.charAt(index))) {
                        value += String.valueOf(newMessage.charAt(index));

                    } else {
                        error = true;
                    }

                    // Si es NUMERICAL_CHAR pertence al valor y es un numero
                } else if (notificationFormat.charAt(index) == NUMERICAL_CHAR) {
                    valueIsNumber = true;
                    value += String.valueOf(newMessage.charAt(index));

                    // Si no es que es parte del id
                } else {
                    id += String.valueOf(newMessage.charAt(index));
                }
            }

            if ((id.isEmpty()) || (value.isEmpty())) {
                error = true;
            }

            if (valueIsNumber) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    error = true;
                }
            }

            if (!error) {
                isNotification = true;
                HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, id);
                String oid = Configuration.getInstance().getOidForNotifications(hwElementId);
                String prefix = notificationData.getMessage();
                snmpAgent.sendSnmpNotification(oid, (prefix + " " + value), prefix);
            }
        }

        // Si no es un mensaje de estado
        if ((!isNotification) && (!isAnswerCommand)) {
            this.processQueryMessage(answerQueryformat, newMessage, faceId, cardId, snmpAgent);
        }
    }
    
    
    protected void processQueryMessage(String answerQueryformat, String newMessage,
            String faceId, String cardId, SnmpAgent snmpAgent){
        boolean errorInNewMessage = false;

        if (answerQueryformat != null) {
                try {

                    String idOfQueryResponse = "", value = "";
                    boolean parsingValue = false;
                    boolean valueIsNumber = false;

                   
                    // Recorremos posicion a posicion
                    for (int index = 0; index < newMessage.length(); index++) {
                        
                        // Si es NUMERICAL_CHAR pertence al valor y es un numero
                        if (answerQueryformat.charAt(index) == NUMERICAL_CHAR) {
                            parsingValue = true;
                            valueIsNumber = true;
                            value += String.valueOf(newMessage.charAt(index));

                        // Si es BLANK_CHAR no pertenece al valor
                        } else if (answerQueryformat.charAt(index) == BLANK_CHAR) {
                            // No se hace nada porque es un separador
                            
                        // Si es SYMBOL tambien pertenece al valor y es un numero
                        } else if (answerQueryformat.charAt(index) == SYMBOL) {
                            parsingValue = true;
                            valueIsNumber = true;

                            // Comprobamos que sea un signo positivo o negativo
                            if ((newMessage.charAt(index) == POSITIVE_SIGNED_CHAR) || (newMessage.charAt(index) == NEGATIVE_SIGNED_CHAR)) {
                                value += String.valueOf(newMessage.charAt(index));

                                // Si no es signo metemos una letra para que falle al terminar de procesar la etiqueta
                            } else {
                                value += "R"; // Puede ser cualquier letra
                            }

                            // Si es ALPHABETICAL_CHAR pertenece al valor pero NO es un numero    
                        } else if (answerQueryformat.charAt(index) == ALPHABETICAL_CHAR) {
                            parsingValue = true;
                            value += String.valueOf(newMessage.charAt(index));

                            //Si es NUMERICAL_STREAM es que a continuación vienen n bytes 
                        } else if (answerQueryformat.charAt(index) == NUMERICAL_STREAM) {
                            try {
                                int number = Integer.parseInt(answerQueryformat.substring((index + 1)));
                                value = newMessage.substring(index);

                                if ((number + index) != newMessage.length()) {
                                    errorInNewMessage = true;
                                    LOG.error("Se descarta el mensaje porque la longitud (" + newMessage.length()
                                            + ") no coincide con el del  formato (" + answerQueryformat.length() + ") : " + newMessage);

                                } else {
                                    index = newMessage.length();
                                }

                            } catch (NumberFormatException ex) {
                                LOG.error("El mensaje no coincide con el formato especificado: " + newMessage);
                                LOG.error(ex);
                            }

                        // Si no, es parte del id
                        } else {

                            if (parsingValue) {
                                this.processNewValue(faceId, cardId, idOfQueryResponse, value, valueIsNumber, snmpAgent);
                                parsingValue = false;
                                valueIsNumber = false;
                                idOfQueryResponse = "";
                                value = "";
                            }

                            idOfQueryResponse += String.valueOf(newMessage.charAt(index));
                        }
                    }

                    if (!errorInNewMessage) {
                        this.processNewValue(faceId, cardId, idOfQueryResponse, value, valueIsNumber, snmpAgent);
                        ///LOG.debug("Message matches the format. AnswerQueryformat [" + AnswerQueryformat + "] message [" + newMessage + "] carId: [" + cardId + "]");
                    }

                } catch (IndexOutOfBoundsException ex) {
                    LOG.error("Longitud erronea del mensaje recibido: " + newMessage);
                }

            } else {
                /////log.error("Message does not match the format. Format: " + AnswerQueryformat + " message: " + newMessage + " carId: " + cardId);
                LOG.error("Message does not match the format. AnswerQueryformat [null] message [" + newMessage + "] carId: [" + cardId + "]");
            }
    }
    
}
