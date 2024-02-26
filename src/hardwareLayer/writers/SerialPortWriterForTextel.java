/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hardwareLayer.writers;

import configuration.data.QueryData;
import configuration.data.ScheduledQueryList;
import hardwareLayer.HardwareDataProcessor;
import java.util.ArrayList;
import jssc.SerialPort;
import snmpAgent.SnmpAgent;

/**
 *
 * @author TEXTEL
 */
public class SerialPortWriterForTextel extends AbstractWriter{

    public SerialPortWriterForTextel(SerialPort port, HardwareDataProcessor processor, String id, String face, SnmpAgent agent) {
        super(port, processor, id, face, agent);
    }


    @Override
    public void setScheduledQueries(ArrayList<ScheduledQueryList> newScheduledQueries){
        scheduledQueries = newScheduledQueries;

        for(ScheduledQueryList currentList : scheduledQueries){
            
            for(QueryData currentQuery : currentList.getQueryList()){
                currentQuery.setQueryForTextelProtocol();
            }
        }
    }
}
