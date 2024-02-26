/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hardwareLayer.readers;

import jssc.SerialPort;
import jssc.SerialPortEventListener;

/**
 *
 * @author TEXTEL
 */
public abstract class AbstractReader implements SerialPortEventListener{

    public abstract void updateSerialPort(SerialPort port);

    public abstract SerialPort getSerialPort();
}
