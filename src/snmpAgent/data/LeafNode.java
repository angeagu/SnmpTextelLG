/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;

import configuration.Configuration;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import snmpAgent.MIBParser;
import snmpAgent.SnmpAgent;
import snmpAgent.Velo;


/**
 *
 * @author Tecnovision
 */
public class LeafNode extends Node{
    
    private static final Logger LOG = Logger.getLogger(LeafNode.class.getName());

    public final static String READ_ONLY = "read-only";
    public final static String READ_WRITE = "read-write";
    public final static String READ_CREATE = "read-create";
    public final static String ACCESSIBLE_FOR_NOTIFY = "accessible-for-notify";
    public final static String NOT_ACCESSIBLE = "not-accessible";
    
    // Info del MIB
    private final String mibNodeType;
    private final String syntax;
    private final String access;
    private final String status;
    private final String description;

    // Informacion adicional para la funcionalidad del agente
    private SoftwareNotificationCondition swNotificationCondition;  // Condicion de notificacion software
    private ArrayList<GUIInfoUpdater> guiInfoUpdaters;              // Elementos a actualizar de la interfaz de usuario
    private Velo velo;                                              // Unicamente para el nodo que controla la luminosidad
    private AdminTextManager adminTextManager;                      // Manager del texto que manda el administrador
    private List<SoftwareSnmpSet> swSnmpSetList;                    // Lista de comandos set que ejecutar bajo determinadas situaciones
    
    // Valor del nodo
    String value;
    
    // Este contador solo afecta a las notificaciones SW
    // Indica el numero de veces que tenemos que detectar el error para mandar notificacion
    private int counterForRetries;


    public LeafNode(String syn, String acc, String sta, String des, String posInParent, String nameId, String type){
        super(posInParent, nameId);
        syntax = syn;
        access = acc;
        status = sta;
        description = des;
        value = null;
        mibNodeType = type;
        velo = null;
        counterForRetries = 0;
        guiInfoUpdaters = new ArrayList<>();
        swSnmpSetList = new ArrayList<>();
        String infoDisplayValue = Configuration.getInstance().getInfoDisplay(nameId);

        /* Esto es para la infomacion de linea, anden, etc */
        if(infoDisplayValue != null){
            value = infoDisplayValue;
            
        }else if(nameId.equals(MIBParser.NOMBRE_COMPLETO)){
            value = "Textel Marimon SA";
        }
    }
    
    @Override
    public void setOid(String currentOid){
        oid = currentOid + ".0";

        // Obtenemos informacion por defecto del nodo
        String infoDisplayValue = Configuration.getInstance().getInfoDisplay(oid);
        
        /* Esto es para la infomacion de linea, anden, etc */
        if(infoDisplayValue != null){
            value = infoDisplayValue;
            
        }else if(nameId.equals(MIBParser.NOMBRE_COMPLETO)){
            value = "Textel Marimon SA";
        }

        // Lo hacemos en este punto porque antes no conoce su OID
        this.setGuiInfoUpdaters();
        this.setSwNotificationCondition();
        this.setAdminTextManager();
        this.setSoftwareSnmpSet();
    }

    private void setSoftwareSnmpSet() {
        swSnmpSetList = Configuration.getInstance().getSoftwareSnmpSet(oid);
    }

    private void setSwNotificationCondition(){
        swNotificationCondition = Configuration.getInstance().getSoftwareNotificationCondition(oid);
    }
    
    private void setGuiInfoUpdaters(){
        guiInfoUpdaters = Configuration.getInstance().getJFrameUpdaters(oid);
    }
    
    private void setAdminTextManager(){
        adminTextManager = Configuration.getInstance().getAdminTextManager(oid);

        if(adminTextManager != null){
            adminTextManager.initialUpdate(oid, value);
        }
    }
    
    @Override
    public LeafNode getLeafNodeByOid(String oid){
        LeafNode node = null;

        if((oid.equals(this.getOid())) || ((oid.equals(this.getOidWithoutPointZero())))){
            node = this;
        }

        return node;
    }
    
    @Override
    public Node getNodeByOid(String oid){
        Node node = null;
        
        if((oid.equals(this.getOid())) || ((oid.equals(this.getOidWithoutPointZero())))){
            node = this;
        }

        return node;
    }
    
    public void setVelo(Velo newVelo){
        velo = newVelo;
    }

    public boolean updateValue(String newValue, SnmpAgent agent){
        boolean sendSwNotification = false;

        /* Si tenemos velo, indicamos la ultima lectura */
        if((velo != null) && (newValue != null) && (newValue.length() > 0)) {
            try {
                int candidateNewBrightness = Integer.parseInt(newValue);
                velo.setBrillo(this.getOid(), candidateNewBrightness, agent);
                
                /* Actualizamos todos los JFrame que tengamos */
                for(GUIInfoUpdater currentUpdater : guiInfoUpdaters){
                    currentUpdater.update(newValue);
                }

            } catch (NumberFormatException ex) {
                LOG.error("La lectura de la luminosidad actual no es un numero"
                        + " entero: " + newValue);
            }
        }

        if(Objects.nonNull(value) && Objects.nonNull(newValue) && !value.equalsIgnoreCase(newValue)) {
            for(SoftwareSnmpSet swSnmpSet : swSnmpSetList) {
                if(!swSnmpSet.check(value, agent) && swSnmpSet.check(newValue, agent)) {
                    LOG.info("Se invoca el software snmp set con oid [" + swSnmpSet.getNotificationOid() + "] y valor [" + swSnmpSet.getLeafNodeValue() + "]");
                    agent.processSetSnmpCommand(swSnmpSet.getNotificationOid(), swSnmpSet.getLeafNodeValue());
                }
            }
        }


        /* Si se produce cambio en el valor, se actualiza el valor */
        if(((value == null) && (newValue != null)) || 
                ((newValue != null) && (value != null) && !(value.equalsIgnoreCase(newValue)))){
            
            /* Dado que hay cambio en el valor, se reinicia el contador */
            counterForRetries = 0;  // Si queremos que solo se envie un unica vez cada error, esta linea va en el else
                                    // de la comprobacion de la notificacion SW, es decir, reinicio el contador cuando
                                    // me doy cuenta que el estado de error se ha terminado
            
            /* Actualizamos el valor del nodo */
            value = newValue;

            /* Actualizamos todos los JFrame que tengamos */
            for(GUIInfoUpdater currentUpdater : guiInfoUpdaters){
                currentUpdater.update(newValue);
            }

        }else if (newValue == null){
            LOG.info("Unexpected value in leaf node: null");
        }


        /* Comprobamos notificaciones software */
        if((swNotificationCondition != null)){
            
            /* Estoy en estado de error */
            if(swNotificationCondition.check(newValue, agent)){
                
                counterForRetries = (counterForRetries <= swNotificationCondition.getRetries()) ?
                                                                (counterForRetries + 1) :
                                                                counterForRetries;
                
                if(counterForRetries == swNotificationCondition.getRetries()){
                    sendSwNotification = true;
                }
            }
        }
        
        
        /* Actualizamos admin text manager */
        if(adminTextManager != null){
            adminTextManager.update(oid, value, agent);
        }

        return sendSwNotification;
    }
    
    public boolean checkSwNotificationCondition(SnmpAgent agent){
        boolean errorBySwNotificationCondition = false;

        if(swNotificationCondition != null){
            errorBySwNotificationCondition = swNotificationCondition.check(value, agent);
        }

        return errorBySwNotificationCondition;
    }
    
    public boolean checkWriteAccess(){
        boolean canWrite;

        if(access.equalsIgnoreCase(READ_ONLY)){
            canWrite = false;

        } else if (access.equalsIgnoreCase(READ_WRITE)) {
            canWrite = true;

        } else if (access.equalsIgnoreCase(READ_CREATE)) {
            canWrite = true;

        } else if (access.equalsIgnoreCase(ACCESSIBLE_FOR_NOTIFY)) {
            canWrite = false;

        } else if (access.equalsIgnoreCase(NOT_ACCESSIBLE)) {
            canWrite = false;

        }else{
            LOG.warn("Unexpected access [" + access + "] for oid [" + oid + "]");
            canWrite = false;
        }
    
        return canWrite;
    }

    @Override
    public Variable getSnmpValue(){
        Variable currentValue = null;
        
        if((value != null) && (value.length() > 0)){
            switch(syntax){

                case "OCTET STRING":
                    currentValue = new OctetString(value);
                    break;

                case "INTEGER":// Dado que no existe en snmp4j la clase Integer, utilizamos la Integer32
                    currentValue = new Integer32(Integer.parseInt(value));
                    break;

                case "Integer32":
                    currentValue = new Integer32(Integer.parseInt(value));
                    break;

                case "IpAddress":
                    currentValue = new IpAddress(value);
                    break;

                case "Counter32":
                    currentValue = new Counter32(Long.parseLong(value));
                    break;

                case "Gauge32":
                    currentValue = new Gauge32(Long.parseLong(value));
                    break;

                case "Unsigned32":
                    currentValue = new UnsignedInteger32(Long.parseLong(value));
                    break;

                case "TimeTicks":
                    currentValue = new TimeTicks(Long.parseLong(value));
                    break;

                case "Opaque":
                    currentValue = new Opaque(value.getBytes(Charset.forName("UTF-8")));
                    break;

                case "Counter64":
                    currentValue = new Counter64(Long.parseLong(value));
                    break;

                default:
                    LOG.error("Unexpected syntax: [" + syntax + "]");
            }
        }

        return currentValue;
    }
   
    private String getOidWithoutPointZero(){
        return oid.substring(0, oid.length() - 2);
    }

    public SoftwareNotificationCondition getSwNotificationCondition() {
        return swNotificationCondition;
    }
    
    public String getStringValue(){
        return value;
    }
    
    @Override
    public void print(){
        LOG.info(nameId + " " + oid + " value: " + value);
    }
}
