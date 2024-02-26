package hardwareLayer.writers;


import static launcher.LAUNCHER.REBOOTING_BEHAVIOUR_ENABLED;

import configuration.Configuration;
import configuration.Utilities;
import configuration.data.HardwareElementIdentifier;
import configuration.data.QueryData;
import configuration.data.ScheduledQueryList;
import hardwareLayer.HardwareDataProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tecnovision
 */
public abstract class AbstractWriter extends TimerTask {

    public static String TMP_FOLDER;
    public final static String TMP_APP_PREFIX = "tmp_app_";
    public final static String TMP_PC_PREFIX = "tmp_pc_";
    public final static String USB = "usb";
    
    private boolean isDataProcessorInitialized;
    public boolean rebootingBehaviourActive;
    public final String tmp_app_file;
    public final String tmp_pc_file;
    private int counterToRestartApp;
    private boolean canWrite;
    private final String faceId;
    protected final String cardId;
    private SerialPort mySerialPort;
    private final SnmpAgent snmpAgent;
    private final HardwareDataProcessor dataProcessor;
    protected ArrayList<ScheduledQueryList> scheduledQueries;
    private final Object mutexForScheduledQueries;
    
    private final int secondsToRestartApp;
    private final int attemptsToRestartAppBeforeRestartPc;
    private final int maxAttemptsToRestartPc;
    
    private static final Logger LOG = Logger.getLogger(AbstractWriter.class.getName());


    public AbstractWriter (
            SerialPort port,
            HardwareDataProcessor processor,
            String id,
            String face,
            SnmpAgent agent){
        
        TMP_FOLDER = Configuration.getInstance().getDriveForTempFolder() + ":\\temp_snmp\\";
        isDataProcessorInitialized = false;
        rebootingBehaviourActive = true;
        counterToRestartApp = 0;
        canWrite = true;
        mySerialPort = port;
        dataProcessor = processor;
        cardId = id;
        faceId = face;
        snmpAgent = agent;
        tmp_app_file = TMP_FOLDER + TMP_APP_PREFIX + faceId + "_" + cardId + ".txt";
        tmp_pc_file = TMP_FOLDER + TMP_PC_PREFIX + faceId + "_" + cardId + ".txt";
        scheduledQueries = new ArrayList<>();
        mutexForScheduledQueries = new Object();
        secondsToRestartApp = Configuration.getInstance().getSecondToRestartApp();
        attemptsToRestartAppBeforeRestartPc = Configuration.getInstance().getAttempsToRestartAppBeforeRestartPc();
        maxAttemptsToRestartPc = Configuration.getInstance().getMaxAttemptsToRestartPcByCard();
    }

    public abstract void setScheduledQueries(ArrayList<ScheduledQueryList> newScheduledQueries);
    
    @Override
    public void run() {
        if (mySerialPort != null){
            counterToRestartApp = 0;

            if ((canWrite) && (isDataProcessorInitialized)) {

                /* Comprobamos si hay que mandar queries y en en dicho caso, se mandan */
                synchronized(mutexForScheduledQueries){
                    for (ScheduledQueryList currentQueryList : scheduledQueries) {
                        if(currentQueryList.check()){
                            this.sendQueries(currentQueryList.getQueryList());
                        }
                    }
                    mutexForScheduledQueries.notifyAll();
                }
            }

        /* Aunque no tenga puerto, espero a que se inicialice toda la aplicacion para intentar recuperarme */
        }else if(isDataProcessorInitialized){
            counterToRestartApp = (rebootingBehaviourActive) ? (counterToRestartApp + 1) : counterToRestartApp;

            /* Se va a reiniciar la APP o el PC */
            if(REBOOTING_BEHAVIOUR_ENABLED && rebootingBehaviourActive && counterToRestartApp >= secondsToRestartApp){
                
                int numberOfTimesAPPWasRebooted = Utilities.getInstance().getCurrentCounter(tmp_app_file, true);
                int numberOfTimesPCWasRebooted = Utilities.getInstance().getCurrentCounter(tmp_pc_file, true);
                rebootingBehaviourActive = (numberOfTimesPCWasRebooted < maxAttemptsToRestartPc);

                if(rebootingBehaviourActive){
                    /* Reinicamos PC */
                    if(numberOfTimesAPPWasRebooted >= attemptsToRestartAppBeforeRestartPc){
                        counterToRestartApp = 0;
                        snmpAgent.closeAndRestartPc(numberOfTimesPCWasRebooted, tmp_pc_file, TMP_APP_PREFIX, TMP_FOLDER);

                    /* Reiniciamos APP */
                    }else{
                        counterToRestartApp = 0;
                        snmpAgent.closeAndRestartApp(numberOfTimesAPPWasRebooted, tmp_app_file);
                    }
                }

            }else{
                long tStart = System.currentTimeMillis();                
                mySerialPort = dataProcessor.reconfigureSerialPort(mySerialPort, cardId);
                long tEnd = System.currentTimeMillis();
                long tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000.0;
                System.out.println("reconfigureSerialPort: " + elapsedSeconds);
                
                
                
                boolean cardOnLine = this.updateUsbStatus();
                
                /* Nos hemos recuperado */
                if((cardOnLine) && (mySerialPort != null)){
                    counterToRestartApp = 0;
                    rebootingBehaviourActive = true;
                    Utilities.getInstance().deleteFile(tmp_app_file);
                    Utilities.getInstance().deleteFile(tmp_pc_file);
                    Utilities.getInstance().deleteFolder(TMP_FOLDER);
                    snmpAgent.checkCandSendSnmpNotification(); // Importante que sea despues de borrar los ficheros!!!
                }
            }
        }
    }
    
    public void sendQueries(ArrayList<QueryData> queriesList){
        
        for (Iterator<QueryData> it = queriesList.iterator(); it.hasNext();) {
            
            try {
                String message = it.next().getQueryToSend(snmpAgent);

                if((mySerialPort != null) && (!message.isEmpty())){

                    boolean success = mySerialPort.writeBytes(message.getBytes());

                    if (!success) {
                        mySerialPort.closePort();
                        mySerialPort = dataProcessor.reconfigureSerialPort(mySerialPort, cardId);
                        this.updateUsbStatus();
                        break;
                    }
                }

            } catch (Exception ex) {
                LOG.error(ex);
            }
        }
    }
    
    public boolean updateUsbStatus(){
        boolean cardOnLine = false;
        HardwareElementIdentifier id = new HardwareElementIdentifier(faceId, cardId, USB);
        String oid = Configuration.getInstance().getOidForQueries(id);
        String newValue = dataProcessor.getCardIdToPortNameValue(cardId);
        newValue = (newValue == null) ? "" : newValue;
        
        /* Actualizamos el valor en el agente */
        if (oid != null) {
            snmpAgent.updateInfo(oid, newValue);
        } else {
            LOG.error("No se ha encontrado un oid para " + id);
        }

        /* En el momento que tengamos texto como valor, significará
           que la tarjeta se ha recuperado, por lo tanto, reiniciamos
           todas las queries (and commands) con onStart activado */
        if(newValue.length() > 0){
            cardOnLine = true;
            this.restartOnStartQueries();
        }
        
        return cardOnLine;
    }

    public void restartOnStartQueries(){
       synchronized(mutexForScheduledQueries){
            for (ScheduledQueryList currentQueryList : scheduledQueries) {
                currentQueryList.restartOnStartQueries();
            }
            mutexForScheduledQueries.notifyAll();
        }
    }

    public SerialPort getSerialPort(){
        return mySerialPort;
    }
    
    public void setCanWrite(boolean value){
        canWrite = value;
    }
    
    public void closeSerialPort(){
        try {
            if(mySerialPort != null){
                mySerialPort.closePort();
                mySerialPort = null;
            }
        } catch (SerialPortException ex) {
            LOG.error(ex);
        }
    }

    public boolean checkCanSendSnmpNotification(){
        boolean candSendNotification;

        /* ¿Nos hemos recuperado? */
        // Esto lo ponemos para saber si al reiniciar el ordenador esta todo recuperado
        if ((mySerialPort != null)) {
            counterToRestartApp = 0;
            rebootingBehaviourActive = true;
            Utilities.getInstance().deleteFile(tmp_app_file);
            Utilities.getInstance().deleteFile(tmp_pc_file);
            Utilities.getInstance().deleteFolder(TMP_FOLDER);
        }

        /* Leemos el estado de nuestros ficheros */
        int numberOfTimesAPPWasRebooted = Utilities.getInstance().getCurrentCounter(tmp_app_file, false);
        int numberOfTimesPCWasRebooted = Utilities.getInstance().getCurrentCounter(tmp_pc_file, false);

        /* Obtenemos estado global de la tarjeta */
        candSendNotification = (numberOfTimesAPPWasRebooted == -1) && (numberOfTimesPCWasRebooted == -1);
        
        rebootingBehaviourActive = (numberOfTimesPCWasRebooted < maxAttemptsToRestartPc);
        
        if((!candSendNotification) && (!rebootingBehaviourActive)){
            candSendNotification = true;
        }
        
        if((numberOfTimesAPPWasRebooted > 0) || (numberOfTimesPCWasRebooted > 0)){
            LOG.info("APP rebooted [" + numberOfTimesAPPWasRebooted + "] PC rebooted [" +
                    numberOfTimesPCWasRebooted + "] by [" + faceId + " - " + cardId + "]");
        }

        return candSendNotification;
    }
    
    public void processorIsInitialized(){
        isDataProcessorInitialized = true;
        this.updateUsbStatus();
    }
    
}