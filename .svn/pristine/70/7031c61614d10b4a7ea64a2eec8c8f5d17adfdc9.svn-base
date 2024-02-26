/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.smi.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author Tecnovision
 */
public class ReceivingSnmpMessages implements CommandResponder{
    
    
    String securityName;
    Address peerAddress;
    OID oid;
    Snmp snmp;
    ResponseListener listener;
     
    public static void main(String[] args){
        ReceivingSnmpMessages p = new ReceivingSnmpMessages();
        try {
            p.a();
        } catch (IOException ex) {
            Logger.getLogger(ReceivingSnmpMessages.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void a() throws IOException {

       
        
        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/161"));
        snmp = new Snmp(transport);
       
        byte[] localEngineID = ((MPv3) snmp.getMessageProcessingModel(MessageProcessingModel.MPv3)).createLocalEngineID();
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(localEngineID), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.setLocalEngine(localEngineID, 0, 0);
       
        snmp.addCommandResponder(this);
        snmp.listen();
        
        
        
        listener = new ResponseListener() {
            public void onResponse(ResponseEvent event) {
                // Always cancel async request when response has been received
                // otherwise a memory leak is created! Not canceling a request
                // immediately can be useful when sending a request to a broadcast
                // address.
                ((Snmp) event.getSource()).cancel(event.getRequest(), this);
                PDU response = event.getResponse();
                PDU request = event.getRequest();
                if (response == null) {
                    System.out.println("Request " + request + " timed out");
                } else {
                    System.out.println("Received response " + response + " on request "
                            + request);
                }
            }
        };
        
                
        
        while(true){}       
        
        //snmp.close();
    }
    
    public synchronized void processPdu(CommandResponderEvent e) {
        PDU command = e.getPDU();
        securityName = new String(e.getSecurityName(), StandardCharsets.ISO_8859_1);
        peerAddress = e.getPeerAddress();
        System.out.println(peerAddress);

        
        if (command != null){

            if (command.size() == 1) {
                
                oid = command.get(0).getOid();
                String value = command.get(0).getVariable().toString();
                
                switch (command.getType()) {
                    case PDU.GET:
                        System.out.println("Hemos recibido un GET con oid " + oid + " y valor " + value);
                        
                        
                        // Configuramos el target
                        CommunityTarget target = new CommunityTarget();
                        target.setCommunity(new OctetString(securityName));
                        target.setAddress(peerAddress);
                        target.setVersion(SnmpConstants.version2c);

                        try {
                            //Configuramos el pdu
                            PDU request = new PDU();
                            VariableBinding var = new VariableBinding(oid, new Integer32(150));
                            request.add(var); // sysDescr
                            request.setType(PDU.RESPONSE);
                            request.setRequestID(command.getRequestID());
                            snmp.send(request, target, null, listener);
                            System.out.println("respuesta enviada: " + request + " al target " + target);

                        } catch (IOException ex) {
                            System.out.println(ex);
                        }
                        
                        
                        break;

                    case PDU.GETNEXT:
                        System.out.println("Hemos recibido un GETNEXT con oid " + oid + " y valor " + value);
                        break;

                    case PDU.GETBULK:

                        System.out.println("Hemos recibido un GETBULK con oid " + oid + " y valor " + value);
                        break;

                    case PDU.SET:
                        System.out.println("Hemos recibido un SET con oid " + oid + " y valor " + value);
                        break;
                        
                    default:
                        System.out.println("Se ha recibido un mensaje no esperado");
                }

            }else{
                System.out.println("ERROR: hemos recibido mas de una variable");
            }
        }
    }

    
    
}
