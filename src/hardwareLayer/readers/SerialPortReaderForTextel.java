package hardwareLayer.readers;


import configuration.Configuration;
import hardwareLayer.HardwareDataProcessor;
import hardwareLayer.SerialPortMessage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import org.apache.log4j.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tecnovision
 */
public class SerialPortReaderForTextel extends AbstractReader {
    
    private final String faceId;
    private final String cardId;
    private String currentMessage;
    private SerialPort mySerialPort;
    private final HardwareDataProcessor dataProcessor;
    
    private final String PROTOCOL = HardwareDataProcessor.TEXTEL_PROTOCOL;
    private static final Logger LOG = Logger.getLogger(SerialPortReaderForTextel.class.getName());
    
    
    public SerialPortReaderForTextel (SerialPort port, String face, String card, HardwareDataProcessor processor){
        mySerialPort = port;
        currentMessage = "";
        cardId = card;
        faceId = face;
        dataProcessor = processor;
    }

    @Override
    public void updateSerialPort(SerialPort port){
        mySerialPort = port;
    }
    
    
    @Override
    public SerialPort getSerialPort(){
        return mySerialPort;
    }
    
   
    @Override
    public void serialEvent(SerialPortEvent event) {

        if (event.isRXCHAR()) {
            int buffer_size = event.getEventValue();   // if isRXCHAR => getEventValue() = bytes count in input buffer 

            try {
                byte buffer[] = mySerialPort.readBytes(buffer_size);

                for(int i = 0; i < buffer.length; i++){

                    char data = (char) buffer[i]; // Pasamos del valor ascii a su representacion

                    /* Recibimos STX */
                    if (data == Configuration.STX) {

                        if(currentMessage.length() > 0){
                            LOG.info("Se descarta el mensaje parcial [" + 
                                    currentMessage + "] por haber recibido un nuevo STX");
                        }

                        currentMessage = "";

                    /* Recibimos ETX */
                    } else if (data == Configuration.ETX) {

                        if(currentMessage.length() > 0){

                            try {
                                //log.info("Encolando: " + currentMessage);
                                dataProcessor.enqueueSerialPortMessage(new SerialPortMessage(cardId, currentMessage, PROTOCOL));

                            } catch (InterruptedException ex) {
                                LOG.error(faceId + ":" + cardId + ": Error al insertar"
                                        + " un nuevo mensaje en el Hardware Data Processor:");
                                LOG.error(ex);
                            }
                        }

                        currentMessage = "";

                    /* Recibimos datos */
                    } else {
                        currentMessage += String.valueOf(data);
                    }
                }

            } catch (SerialPortException ex) {
                LOG.error(ex);
            }
        } 
    }
}
