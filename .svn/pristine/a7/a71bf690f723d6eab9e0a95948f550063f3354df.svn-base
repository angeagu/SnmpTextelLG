/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import configuration.Configuration;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

/**
 *
 * @author Tecnovision
 */
public class SerialPortTest {
    
    public static void main(String[] args){
        SerialPortTest test = new SerialPortTest();
        
        String[] portNames = SerialPortList.getPortNames();

        // buscamos qué puertos serie tienen alguna tarjeta conectada
        for (String serialPortName : portNames) {
            try {
                System.out.println("Voy a preguntar quien está en el puerto " + serialPortName);
                test.writeAndRead(serialPortName);
            } catch (SerialPortTimeoutException | SerialPortException ex) {
                System.out.println("Desestimado el puerto " + serialPortName + " al no recibir respuesta");
            }
        }
        
    }
    
    
    private void writeAndRead(String portName) throws SerialPortException, SerialPortTimeoutException {
        // Abrimos y configuramos el puerto serie
        SerialPort serialPortAux = new SerialPort(portName);
        serialPortAux.openPort();
        serialPortAux.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // Mandamos la pregun Q
        String message = Configuration.STX + "Q?" + Configuration.ETX;
        serialPortAux.writeBytes(message.getBytes());

        // Preparo mi buffer (variable 'name')
        String name = new String();
        boolean start = false;
        boolean end = false;

        // Leemos la respuesta de byte en byte hasta que hayamos leido un ETX
        while (!end) {

            byte[] tmpBuffer = serialPortAux.readBytes(1,  1000);
            char data = (char) tmpBuffer[0]; // Pasamos del valor ascii a su representacion

            if (data == Configuration.STX) {
                start = true;

            } else if (data == Configuration.ETX) {
                end = true;

            } else {
                name += String.valueOf(data);
            }
        }

        // Cierro el puerto
        serialPortAux.closePort();

        // Si el mensaje está completo
        if (start && end) {
            // Alimento el Hashmap con un nuevo registro
            System.out.println(name + " at " + portName);
        }
    }
}
