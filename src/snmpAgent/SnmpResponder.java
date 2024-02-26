/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 *
 * @author Tecnovision
 */
public class SnmpResponder implements CommandResponder {

    public final static int RETRIES_TO_SEND_RESPONSE = 2;
    public final static int TIMEOUT_TO_SEND_RESPONSE = 1000;
    public final static int MAX_PDU_LENGTH_IN_BYTES = 1460;
    
    private final SnmpAgent agent;
    private final Snmp snmp;
    private final ResponseListener listener;
    private static final Logger log = Logger.getLogger(SnmpResponder.class.getName());

    public SnmpResponder(SnmpAgent snmpAgent, Snmp currentSnmp) {
        agent = snmpAgent;
        snmp = currentSnmp;

        listener = new ResponseListener() {
            @Override
            public void onResponse(ResponseEvent event) {
                // Always cancel async request when response has been received
                // otherwise a memory leak is created! Not canceling a request
                // immediately can be useful when sending a request to a broadcast
                // address.
                ((Snmp) event.getSource()).cancel(event.getRequest(), this);
                PDU response = event.getResponse();
                PDU request = event.getRequest();
                if (response == null) {
                    log.info("Request " + request + " timed out");
                } else {
                    log.info("Received response " + response + " on request "
                            + request);
                }
            }
        };
    }

    @Override
    public synchronized void processPdu(CommandResponderEvent event) {
        int snmpVersion = event.getMessageProcessingModel();
        PDU command = event.getPDU();
        String securityName = new String(event.getSecurityName(), StandardCharsets.ISO_8859_1);

        if (command != null) {

            if (command.size() >= 1) {

                OID oid = command.get(0).getOid();

                // Configuramos el target - V1 y V2
//                
//                CommunityTarget target = new CommunityTarget();
//                target.setCommunity(new OctetString(securityName));
//                target.setAddress(event.getPeerAddress());
//                target.setVersion(SnmpConstants.version2c);
//                target.setRetries(RETRIES_TO_SEND_RESPONSE);
//                target.setTimeout(TIMEOUT_TO_SEND_RESPONSE);
            
                // Configuramos el target - V3

                UserTarget target = new UserTarget();
                Address targetAddress = event.getPeerAddress();
                UdpAddress udpAddress = (UdpAddress) targetAddress;
                target.setAddress(new UdpAddress(udpAddress.getInetAddress(), udpAddress.getPort()));
                target.setRetries(RETRIES_TO_SEND_RESPONSE);
                // set timeout to 500 milliseconds: 2*500ms = 1s total timeout
                target.setTimeout(TIMEOUT_TO_SEND_RESPONSE);
                target.setVersion(SnmpConstants.version3);
                target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                target.setSecurityName(new OctetString(securityName));
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
                
                //Aceptamos solo version SNMP3
                if (snmpVersion != 3) {
                    //Configuramos el pdu
                    PDU response = event.getPDU();
                    Variable value = new OctetString("Only SNMPv3 is allowed");
                    OID oidForGetRequest = new OID(this.withoutPointZero(oid.toString()));
                    VariableBinding var = (value != null) ? new VariableBinding(oidForGetRequest, value) :
                                                            new VariableBinding(oidForGetRequest);
                    response.add(var);
                    response.setType(PDU.RESPONSE);
                    response.setRequestID(command.getRequestID());

                    try {
                        //Enviamos respuesta - SNMP V1 y V2
                        //snmp.send(response, target, null, listener);
                        
                        //Enviamos respuesta - SNMP V3
                        event.getMessageDispatcher().returnResponsePdu(
                            event.getMessageProcessingModel(),
                            event.getSecurityModel(),
                            event.getSecurityName(),
                            event.getSecurityLevel(),
                            response,
                            event.getMaxSizeResponsePDU(),
                            event.getStateReference(),
                            new StatusInformation());
                        

                    } catch (IOException ex) {
                        log.error(ex);
                    }

                    log.info(response);
                    return;
                }

                switch (command.getType()) {

                    case PDU.GET:
                        log.debug("Hemos recibido un GET con oid " + oid);

                        //Configuramos el pdu
                        //PDU response = new PDU();
                        PDU response = event.getPDU();
                        Variable value = agent.getInfoByOid(oid.toString());
                        OID oidForGetRequest = new OID(this.withoutPointZero(oid.toString()));
                        VariableBinding var = (value != null) ? new VariableBinding(oidForGetRequest, value) :
                                                                new VariableBinding(oidForGetRequest);
                        //response.add(var);
                        response.get(0).setVariable(value);
                        response.setType(PDU.RESPONSE);
                        response.setRequestID(command.getRequestID());

                        try {
                            //snmp.send(response, target, null, listener);
                            
                            snmp.getMessageDispatcher().returnResponsePdu(
                            event.getMessageProcessingModel(),
                            event.getSecurityModel(),
                            event.getSecurityName(),
                            event.getSecurityLevel(),
                            response,
                            event.getMaxSizeResponsePDU(),
                            event.getStateReference(),
                            new StatusInformation());

                        } catch (IOException ex) {
                            log.error(ex);
                        }

                        log.debug(response);

                        break;

                    case PDU.GETNEXT:
                        log.info("Hemos recibido un GETNEXT con oid " + oid);

                        //Configuramos el pdu
                        //response = new PDU();
                        response = event.getPDU();
                        var = agent.getNextVariableBinding(oid.toString());
                        //response.add(var);
                        response.get(0).setVariable(var.getVariable());
                        
                        response.setType(PDU.RESPONSE);
                        response.setRequestID(command.getRequestID());

                        try {
                            //snmp.send(response, target, null, listener);
                            
                            snmp.getMessageDispatcher().returnResponsePdu(
                            event.getMessageProcessingModel(),
                            event.getSecurityModel(),
                            event.getSecurityName(),
                            event.getSecurityLevel(),
                            response,
                            event.getMaxSizeResponsePDU(),
                            event.getStateReference(),
                            new StatusInformation());

                        } catch (IOException ex) {
                            log.error(ex);
                        }

                        log.info(response);

                        break;

                    case PDU.GETBULK:
                        //https://www.webnms.com/snmp/help/snmpapi/snmpv3/snmp_operations/snmp_getbulk.html

                        log.info(command);

                        int nonRepeaters = command.getNonRepeaters();
                        int maxRepetitions = command.getMaxRepetitions();

                        int n = Math.min(nonRepeaters, command.size());
                        int r = Math.max(command.size(), 0);

                        //Configuramos el pdu
                        ArrayList<PDU> responseList = new ArrayList<>();
                        //PDU currentResponse = new PDU();
                        PDU currentResponse = event.getPDU();

                        responseList.add(currentResponse);

                        currentResponse.setType(PDU.RESPONSE);
                        currentResponse.setRequestID(command.getRequestID());

                        // Primero realizamos los n get next
                        for (int i = 0; i < n; i++) {
                            OID currentOid = command.get(i).getOid();
                            var = agent.getNextVariableBinding(currentOid.toString());
                            
                            // Si no podemos meter mas variables, creamos un nuevo PDU
                            if((currentResponse.getBERLength() + var.getBERLength()) > MAX_PDU_LENGTH_IN_BYTES){
                                currentResponse = new PDU();
                                responseList.add(currentResponse);
                                currentResponse.setType(PDU.RESPONSE);
                                currentResponse.setRequestID(command.getRequestID());
                            }

                            currentResponse.add(var);
                        }

                        // Recorremos las variables restantes
                        for (int i = n; i < r; i++) {
                            OID currentOid = command.get(i).getOid();

                            for (int j = 0; j < maxRepetitions; j++) {
                                var = agent.getNextVariableBinding(currentOid.toString());
                                
                                // Si no podemos meter mas variables, creamos un nuevo PDU
                                if((currentResponse.getBERLength() + var.getBERLength()) > MAX_PDU_LENGTH_IN_BYTES){
                                    currentResponse = new PDU();
                                    responseList.add(currentResponse);
                                    currentResponse.setType(PDU.RESPONSE);
                                    currentResponse.setRequestID(command.getRequestID());
                                }
                                
                                currentResponse.add(var); // sysDescr
                            }
                        }

                        try {
                            
                            for(PDU currentPDU : responseList){
                                //snmp.send(currentPDU, target, null, listener);
                                    
                                snmp.getMessageDispatcher().returnResponsePdu(
                                event.getMessageProcessingModel(),
                                event.getSecurityModel(),
                                event.getSecurityName(),
                                event.getSecurityLevel(),
                                currentPDU,
                                event.getMaxSizeResponsePDU(),
                                event.getStateReference(),
                                new StatusInformation());

                                log.debug("Se envia el paquete: " + currentPDU);
                            }

                        } catch (IOException ex) {
                            log.error(ex);
                        }

                        break;

                    case PDU.SET:
                        int error;
                        String newValue;

                        if(command.get(0).getVariable().getSyntaxString().equalsIgnoreCase("OCTET STRING")){
                            OctetString aux = (OctetString) command.get(0).getVariable();
                            newValue = new String(aux.getValue(), StandardCharsets.ISO_8859_1);
                            
                        }else{
                            newValue = command.get(0).getVariable().toString();
                        }

                        log.info("Hemos recibido un SET con oid " + oid + " y valor " + newValue);

                        String readWriteCommunity = agent.getCommunity_read_write();
                        
                        /* Si no coincide el community para read/write */
                        if(!(readWriteCommunity.equals(securityName))){
                            error = SnmpConstants.SNMP_ERROR_AUTHORIZATION_ERROR;

                        /* Si el community permite escritura */
                        }else{
                            error = agent.processSetSnmpCommand(oid.toString(), newValue);    ////
                        }

                        //Configuramos el pdu
                        //response = new PDU();
                        response = event.getPDU();
                        response.setType(PDU.RESPONSE);
                        response.setRequestID(command.getRequestID());
                        value = agent.getInfoByOid(oid.toString());
                        var = (value != null) ? new VariableBinding(oid, value) :
                                                new VariableBinding(oid);
                        //response.add(var);
                        response.get(0).setVariable(value);

                        if (error != 0) {
                            response.setErrorStatus(error);
                        }

                        try {
                            //snmp.send(response, target, null, listener);
                            
                            snmp.getMessageDispatcher().returnResponsePdu(
                            event.getMessageProcessingModel(),
                            event.getSecurityModel(),
                            event.getSecurityName(),
                            event.getSecurityLevel(),
                            response,
                            event.getMaxSizeResponsePDU(),
                            event.getStateReference(),
                            new StatusInformation());

                        } catch (IOException ex) {
                            log.error(ex);
                        }

                        break;
                }

            } else {
                System.out.println("ERROR: hemos recibido mas de una variable");
            }
        }
    }

    private String withoutPointZero(String oid) {
        // le quitamos la terminación '.0' al OID que devolvemos empaquetado
        // al Administrador que nos ha hecho un GET

        if (oid.endsWith(".0")) {
            oid = oid.substring(0, oid.length() - 2);
        }
        return oid;
    }
}
