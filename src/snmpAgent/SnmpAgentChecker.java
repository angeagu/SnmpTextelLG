/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

/**
 *
 * @author Tecnovision
 */
public class SnmpAgentChecker extends TimerTask{

    private final SnmpAgent snmpAgent;
    private final ArrayList<String> oids;
    private final String mainOid;
    private final String ok;
    private final String ko;
    
    public SnmpAgentChecker(SnmpAgent agent, String oid, ArrayList<String> oidList,
                            String stringOk, String stringKo){
        snmpAgent = agent;
        oids = oidList;
        mainOid = oid;
        ok = stringOk;
        ko = stringKo;
    }


    @Override
    public void run() {
        boolean error = false;
        String value = ok;
        
        for(Iterator<String> it = oids.iterator(); it.hasNext(); ){
            String currentOid = it.next();

            // Para aumentar la eficiencia
            if(snmpAgent.checkSwNotification(currentOid)){
                error = true;
                break;
            }
        }

        if(error){
            value = ko;
        }

        snmpAgent.updateInfo(mainOid, value);
    }
}
