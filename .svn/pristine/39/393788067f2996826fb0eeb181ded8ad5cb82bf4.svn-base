/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;

import configuration.Configuration;
import java.util.ArrayList;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;
import socket.Client;

/**
 *
 * @author Tecnovision
 */
public class AdminTextTimer extends TimerTask {

    private final Client socket;
    private final ArrayList<String> mainOids;
    private final SnmpAgent agent;
    private static final Logger log = Logger.getLogger(AdminTextTimer.class.getName());
    
    public AdminTextTimer(Client socket, ArrayList<String> mainOids, SnmpAgent agent) {
        this.socket = socket;
        this.agent = agent;
        this.mainOids = mainOids;
    }

    @Override
    public void run() {
        String socketPackage = Configuration.STX + "CMD" + (char) 0x19
                + "CLS$" + Configuration.ETX;
        log.info("Se envia [" + socketPackage + "]");
        if (socket.sendMessage(socketPackage)) {
            log.error("removing the admin text....");
        }

        for(String currentOid : mainOids){
            agent.updateInfo(currentOid, "");
        }

        this.cancel();
    }
}
