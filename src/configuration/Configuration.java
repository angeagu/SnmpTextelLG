package configuration;

import configuration.data.CardIdentifier;
import configuration.data.CheckerData;
import configuration.data.Command;
import configuration.data.HardwareElementIdentifier;
import configuration.data.HardwareNotificationData;
import configuration.data.JFrameCommand;
import configuration.data.QueryData;
import configuration.data.QueryIdentifier;
import configuration.data.ScheduledQueryKey;
import configuration.data.ScheduledQueryList;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.xml.parsers.*;
import org.apache.log4j.Logger;
import org.xml.sax.*;
import org.w3c.dom.*;
import snmpAgent.Velo;
import snmpAgent.data.AdminTextManager;
import snmpAgent.data.GUIInfoUpdater;
import snmpAgent.data.SoftwareNotificationCondition;
import snmpAgent.data.SoftwareSnmpSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Tecnovision
 */
public class Configuration {

    /* esta Clase sigue el patrón Singleton (una única instancia),
     el que se utiliza cuando una serie de datos han de estar disponibles
     para todos los demás objetos de la aplicación, o cuando una Clase controla
     el acceso a un recurso físico único, como un ratón, un puerto serie,
     un fichero abierto en modo exclusivo, etc.
     - variable 'instance' estática y privada
     - constructor privado
     - método 'getInstance' estático y público */
    private static Configuration instance = null;
    private static final Logger log = Logger.getLogger(Configuration.class.getName());

    /* declaramos el primer HashMap (estructura de datos que asocia claves con valores)
     infoDisplay: la clave será el parametro (linea, estacion, etc)... (y también trapIp, trapPort, etc) */
    private final HashMap<String, String> infoDisplay;

    /* key = jframeId and value = hashmap where key = oid and value = GUIIInfoUpdater */
    private final HashMap<String, TreeSet<String>> cardIdsByFaceId;
    private final HashMap<String, String> cardIdToProtocol;
    private final HashMap<String, String> protocolToQueryForCardId;
    private final HashMap<HardwareElementIdentifier, String> oidsForQueries;
    private final HashMap<HardwareElementIdentifier, String> oidsForNotifications;
    private final HashMap<CardIdentifier, HashMap<ScheduledQueryKey, ScheduledQueryList>> scheduledQueries;
    private final HashMap<QueryIdentifier, String> queryResponseFormat;
    private final HashMap<QueryIdentifier, String> commandResponseFormat;
    private final HashMap<QueryIdentifier, HardwareNotificationData> hardwareNotificationData;
    private final HashMap<String, SoftwareNotificationCondition> softwareNotifications;
    private final HashMap<String, CheckerData> agentCheckers; /* Key = mainOid */
    private final HashMap<String, AdminTextManager> adminTextManagers;

    private final HashMap<String, HashMap<String, GUIInfoUpdater>> jframeUpdaters;
    private final HashMap<String, Velo> veils;
    private final HashMap<CardIdentifier, ArrayList<String>> oidsByCardIdentifier;
    private final HashMap<String, Command> commands;
    private final HashMap<String, JFrameCommand> jframeCommands;
    private final HashMap<String, Point> brightnessProfiles;
    private final Map<String, List<SoftwareSnmpSet>> softwareSnmpSetMap;
    private final List<String> ignoredSerialPortNames;

    // Panel description
    private int panelColumns;
    private int panelRows;
    private int moduleWidth;
    private int moduleHeight;
    private int widthInModules;
    private int heightInModules;

    // Behaviour on usb error
    private String driveForTempFolder;
    private int secondToRestartApp;
    private int attempsToRestartAppBeforeRestartPc;
    private int maxAttemptsToRestartPcByCard;

    // Behaviour on cmdLog_Click
    private boolean showingLog;

    // standard messages
    public final static char STX = (char) 0x02;
    public final static char ETX = (char) 0x03;
    public final static char ACK = (char) 0x06;
    public final static char NAK = (char) 0x15;
    public final static char CR  = (char) 0x0D;

    // Switch off Display
    private ArrayList<String> cardIdsForSwitchingOffDisplay;
    private String commandForSwitchingOffDisplay;

    // tags for address.xml
    public final static String INFO = "INFO";
    public final static String BRIGHTNESS_PROFILE = "brightness_profile";

    private Configuration() {
        adminTextManagers = new HashMap<>();
        agentCheckers = new HashMap<>();
        brightnessProfiles = new HashMap<>();
        cardIdsByFaceId = new HashMap<>();
        commands = new HashMap<>();
        hardwareNotificationData = new HashMap<>();
        infoDisplay = new HashMap<>();
        jframeCommands = new HashMap<>();
        jframeUpdaters = new HashMap<>();
        oidsByCardIdentifier = new HashMap<>();
        oidsForNotifications = new HashMap<>();
        oidsForQueries = new HashMap<>();
        queryResponseFormat = new HashMap<>();
        commandResponseFormat = new HashMap<>();
        scheduledQueries = new HashMap<>();
        softwareNotifications = new HashMap<>();
        cardIdToProtocol = new HashMap<>();
        protocolToQueryForCardId = new HashMap<>();
        veils = new HashMap<>();
        softwareSnmpSetMap = new LinkedHashMap<>();
        ignoredSerialPortNames = new ArrayList<>();
    }

    public static Configuration getInstance() {
        /* única manera de conseguir una única instancia (objeto) de esta Clase
         Si no existe, se crea
         Si existe, se devuelve la referencia al objeto instanciado con anterioridad */

        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void readAddress(String xmlFilename) {
        /* leer un archivo 'xml' con la información del panel
         y dejarla en el HashMap 'infoDisplay' para cuando
         alguien la pida (con 'getInfoDisplay') */
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(xmlFilename);
            Element doc = dom.getDocumentElement();

            Loader.getInstance().readIdValueAttributes(doc, Loader.PANEL, 0, INFO);
            
            /* recorremos el documento buscando la etiqueta INFO
             y teniendo en cuenta que sólo puede haber una */
            NodeList displays = doc.getElementsByTagName(Loader.PANEL);
            if (displays.getLength() == 1) {
                Element currentDisplay = (Element) displays.item(0);
                String brightnessProfile = currentDisplay.getAttribute(BRIGHTNESS_PROFILE);
                addInfoToInfoDisplay(BRIGHTNESS_PROFILE, brightnessProfile);

            } else {
                log.error("Wrong number [" + displays.getLength() + "] of INFO objects in xml file");
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            log.error(ex);
        }
    }

    public String getInfoDisplay(String key) {
        /* obtener el valor de cualquiera de las claves leidas del xml anteriormente */
        return infoDisplay.get(key);
    }

    public void addCardIdToFaceId(String faceId, String cardId) {
        /* añadir desde 'Loader' una tarjeta a la lista de tarjetas de una cara concreta */

        /* cardList va a ser un puntero que apunta al valor (TreeSet) de la clave 'faceId'
         en el HashMap de 'lista de tarjetas en cada cara' */
        TreeSet<String> cardList = cardIdsByFaceId.get(faceId);

        /* no añadimos directamente al HashMap por si acaso no está dada de alta todavía esa cara /*
         /* si no existe la lista de tarjetas para una cara concreta, la creamos vacía */
        if (cardList == null) {
            cardList = new TreeSet<>();
            cardIdsByFaceId.put(faceId, cardList);
        }
        /* como es un puntero, por eso ahora puedo añadir a cardList y se añade,
         de forma persistente, al treeSet que tiene el HashMap 'cardIdsByFaceId' */
        cardList.add(cardId);
    }
    
    
    public void addProtocolByCardId(String cardId, String protocol) {////// comentar! 
        cardIdToProtocol.put(cardId, protocol);
    }

    public void addScheduledQuery(CardIdentifier cardIdentifier, int period, int secondsToSendQuery, QueryData query) {
        /* actualizar la lista de scheduled queries por cada tarjeta (de una cara concreta)
         agrupadas por la clave 'periodo + segundos restantes para ejecutar' */

        if ((query != null) && (query.getQuery().length() > 0)) {
            ScheduledQueryKey scheduledQueryKey = new ScheduledQueryKey(period, secondsToSendQuery);

            /* primero hay que obtener EL PUNTERO al hashMap con las scheduledQueries
             que ya tengamos para esa tarjeta */
            HashMap<ScheduledQueryKey, ScheduledQueryList> queriesForCardIdentifier = scheduledQueries.get(cardIdentifier);

            /* si es null, creamos un hashMap vacío y lo damos de alta para esa tarjeta
             así el puntero ya no apuntará a null*/
            if (queriesForCardIdentifier == null) {
                queriesForCardIdentifier = new HashMap<>();
                scheduledQueries.put(cardIdentifier, queriesForCardIdentifier);
            }

            /* ahora hay que obtener EL PUNTERO al objeto scheduledQueryList
             que ya tengamos para esa clave 'periodo + segundos restantes' */
            ScheduledQueryList queriesForScheduledQueryKey = queriesForCardIdentifier.get(scheduledQueryKey);

            /* si es null, instanciamos un objeto de esa clase y lo damos de alta
             en el hashMap con las scheduledQueries para esa tarjeta
             así el puntero ya no apuntará a null */
            if (queriesForScheduledQueryKey == null) {
                queriesForScheduledQueryKey = new ScheduledQueryList(period, secondsToSendQuery);
                queriesForCardIdentifier.put(scheduledQueryKey, queriesForScheduledQueryKey);
            }

            /* por último, como queriesForScheduledQueryKey es el puntero a queriesForCardIdentifier,
             que es el puntero al valor de scheduledQueries con la clave de nuestra tarjeta,
             solamente nos queda añadir el query a queriesForScheduledQueryKey
             para que se añada a scheduledQueries */
            queriesForScheduledQueryKey.addQuery(query);

        } else {
            log.error("Error scheduling query [" + query.getQuery() + "] "
                    + "with period [" + period + "] "
                    + "secondsToSendQuery " + secondsToSendQuery + "] "
                    + "for " + cardIdentifier);
        }
    }

    public void addQueryResponseFormat(QueryIdentifier id, String format) {
        queryResponseFormat.put(id, format);
    }

    public void addCommandResponseFormat(QueryIdentifier id, String format) {
        commandResponseFormat.put(id, format);
    }
    
    public void addOIDForQueries(HardwareElementIdentifier id, String currentOid) {
        /* Actualizamos la lista de oids por elemento hardware */
        oidsForQueries.put(id, currentOid);

        /* Actualizamos la lista de oids por tarjeta hardware */
        CardIdentifier cardIdentifier = new CardIdentifier(id.getCardId(), id.getFaceId());
        ArrayList<String> oidList = oidsByCardIdentifier.get(cardIdentifier);

        if (oidList == null) {
            oidList = new ArrayList<>();
            oidsByCardIdentifier.put(cardIdentifier, oidList);
        }

        oidList.add(currentOid);
    }

    public void addOIDForNotification(HardwareElementIdentifier id, String currentOid) {
        /* Actualizamos la lista de oids por elemento hardware */
        oidsForNotifications.put(id, currentOid);
    }

    public void addNotificationFormat(QueryIdentifier id, HardwareNotificationData data) {
        hardwareNotificationData.put(id, data);
    }

    public void addSoftwareNotification(String oid, SoftwareNotificationCondition sw) {
        softwareNotifications.put(oid, sw);
    }
    
    public void addAdminTextManager(String oid, AdminTextManager admin) {
        adminTextManagers.put(oid, admin);
    }

    public void addChecker(String mainOid, CheckerData data) {
        agentCheckers.put(mainOid, data);
    }

    public void addJFrameUpdater(String jFrameId, String oid, GUIInfoUpdater guiUpdater) {
        HashMap<String, GUIInfoUpdater> updaterForCurrentJFrame = jframeUpdaters.get(jFrameId);
        if (updaterForCurrentJFrame == null) {
            updaterForCurrentJFrame = new HashMap<>();
            jframeUpdaters.put(jFrameId, updaterForCurrentJFrame);
        }

        updaterForCurrentJFrame.put(oid, guiUpdater);
    }

    public void addVeil(String oid, Velo veil) {
        veils.put(oid, veil);
    }

    public HashMap<String, TreeSet<String>> getCardIdsByFaceId() {
        return cardIdsByFaceId;
    }
    
    public String getProtocolByCardId(String cardId) {
        return cardIdToProtocol.get(cardId);
    }
    
    
    public ArrayList<String> getCardIdsByProtocol(String protocol) {
        ArrayList<String> list = new ArrayList<>();
        
        for(Map.Entry<String, String> entry : cardIdToProtocol.entrySet()){
            
            if(entry.getValue().equals(protocol)){
                list.add(entry.getKey());
            }
        }

        return list;
    }
    
    public LinkedHashSet<String> getProtocols(){
        LinkedHashSet<String> list = new LinkedHashSet<>();

        for(String protocol : cardIdToProtocol.values()){
            list.add(protocol);
        }

        return list;
    }

    public ArrayList<ScheduledQueryList> getScheduledQueries(String faceId, String cardId) {
        ArrayList<ScheduledQueryList> queries = new ArrayList<>();
        CardIdentifier cardIdentifier = new CardIdentifier(cardId, faceId);
        HashMap<ScheduledQueryKey, ScheduledQueryList> queriesForCardIdentifier = scheduledQueries.get(cardIdentifier);

        if (queriesForCardIdentifier != null) {
            for (ScheduledQueryList currentList : queriesForCardIdentifier.values()) {
                queries.add(currentList);
            }
        } else {
            log.warn("Atention! no queries found "
                    + "for faceId [" + faceId + "] "
                    + "cardId [" + cardId + "]");
        }

        return queries;
    }

    public String getQueryResponseFormat(String faceId, String cardId, String firstCharOfQuery) {
        QueryIdentifier key = new QueryIdentifier(faceId, cardId, firstCharOfQuery);

        return queryResponseFormat.get(key);
    }
    
    public String getCommandResponseFormat(String faceId, String cardId, String firstCharOfCommand) {
        QueryIdentifier key = new QueryIdentifier(faceId, cardId, firstCharOfCommand);

        return commandResponseFormat.get(key);
    }

    public HardwareNotificationData getHwNotificationData(String faceId, String cardId, String firstCharOfQuery) {
        QueryIdentifier key = new QueryIdentifier(faceId, cardId, firstCharOfQuery);

        return hardwareNotificationData.get(key);
    }

    public SoftwareNotificationCondition getSoftwareNotificationCondition(String oid) {
        return softwareNotifications.get(oid);
    }
    
    public AdminTextManager getAdminTextManager(String oid) {
        return adminTextManagers.get(oid);
    }

    public HashMap<String, CheckerData> getAgentCheckers() {
        return agentCheckers;
    }

    public GUIInfoUpdater getGUIInfoUpdaterByOid(String jFrameId, String oid) {
        GUIInfoUpdater currentGUIUpdater = null;
        HashMap<String, GUIInfoUpdater> updatersForCurrentJFrameId = jframeUpdaters.get(jFrameId);
        if (updatersForCurrentJFrameId != null) {
            currentGUIUpdater = updatersForCurrentJFrameId.get(oid);
        }

        if (currentGUIUpdater == null) {
            log.error("Configuration.getGUIInfoUpdaterByOid: "
                    + "no GUIInfoUpdater found for "
                    + "jFrameId [" + jFrameId + "] "
                    + "and oid [" + oid + "]");
        }

        return currentGUIUpdater;
    }

    public HashMap<String, Velo> getVeils() {
        return veils;
    }

    public Velo getVeil(String oid) {
        return veils.get(oid);
    }

    public String getQueryForCardIdByProtocol(String protocol) {
        return protocolToQueryForCardId.get(protocol);
    }

    public void addQueryForCardId(String protocol, String queryForCardId) {
        String previousQuery = protocolToQueryForCardId.get(protocol);

        if(previousQuery != null && !previousQuery.equals(queryForCardId)){
            log.error("Protocol [" + protocol + "] tried to rewrite queryForCardId [" + previousQuery + "] with [" + queryForCardId + "]. Please check CARD tags");

        }else{
            protocolToQueryForCardId.put(protocol, queryForCardId);
        }
    }

    public ArrayList<String> getOidList(String faceId, String cardId) {
        return oidsByCardIdentifier.get(new CardIdentifier(cardId, faceId));
    }

    public String getOidForQueries(HardwareElementIdentifier id) {
        return oidsForQueries.get(id);
    }

    public String getOidForNotifications(HardwareElementIdentifier id) {
        return oidsForNotifications.get(id);
    }

    public ArrayList<GUIInfoUpdater> getJFrameUpdaters(String oid) {
        ArrayList<GUIInfoUpdater> updaters = new ArrayList<>();

        /* Recorremos todos los JFrame*/
        for (String currentJFrame : jframeUpdaters.keySet()) {

            /* Recorremos todos los updates del jframe actual */
            for (String currentOid : jframeUpdaters.get(currentJFrame).keySet()) {
                if (currentOid.equals(oid)) {
                    updaters.add(jframeUpdaters.get(currentJFrame).get(currentOid));
                }
            }
        }

        return updaters;
    }

    public void addBrightnessProfile(String id, Point point) {
        brightnessProfiles.put(id, point);
    }

    public Point getBrightnessProfile(String id) {
        return brightnessProfiles.get(id);
    }

    public void addCommand(String oid, Command currentCommand) {
        commands.put(oid, currentCommand);
    }

    public Command getCommand(String oid) {
        return commands.get(oid);
    }

    public void addSoftwareSnmpSet(String oid, SoftwareSnmpSet softwareSnmpSet) {
        softwareSnmpSetMap.computeIfAbsent(oid, o -> new ArrayList<>()).add(softwareSnmpSet);
    }

    public List<SoftwareSnmpSet> getSoftwareSnmpSet(String oid) {
        return softwareSnmpSetMap.getOrDefault(oid, new ArrayList<>());
    }

    public void addIgnoredSerialPortNames(String name) {
        ignoredSerialPortNames.add(name);
    }

    public List<String> getIgnoredSerialPortNames() {
        return ignoredSerialPortNames;
    }

    public void addJFrameCommand(String id, JFrameCommand currentCommand) {
        jframeCommands.put(id, currentCommand);
    }

    public JFrameCommand getJFrameCommand(String id) {
        return jframeCommands.get(id);
    }

    public int getPanelColumns() {
        return panelColumns;
    }

    public int getPanelRows() {
        return panelRows;
    }

    public int getModuleWidth() {
        return moduleWidth;
    }

    public int getModuleHeight() {
        return moduleHeight;
    }

    public int getWidthInModules() {
        return widthInModules;
    }

    public int getHeightInModules() {
        return heightInModules;
    }

    public void setBehaviourOnUsbError(int secondToRestartApp, int attempsToRestartAppBeforeRestartPc,
            int maxAttemptsToRestartPcByCard, String drive) {
        this.secondToRestartApp = secondToRestartApp;
        this.attempsToRestartAppBeforeRestartPc = attempsToRestartAppBeforeRestartPc;
        this.maxAttemptsToRestartPcByCard = maxAttemptsToRestartPcByCard;
        this.driveForTempFolder = drive;
    }

    public int getSecondToRestartApp() {
        return secondToRestartApp;
    }

    public int getAttempsToRestartAppBeforeRestartPc() {
        return attempsToRestartAppBeforeRestartPc;
    }

    public int getMaxAttemptsToRestartPcByCard() {
        return maxAttemptsToRestartPcByCard;
    }

    public String getDriveForTempFolder() {
        return driveForTempFolder;
    }

    public void setInfoForSwitchingOffDisplay(ArrayList<String> cardIds, String command) {
        cardIdsForSwitchingOffDisplay = cardIds;
        commandForSwitchingOffDisplay = command;
    }

    public ArrayList<String> getCardIdsForSwitchingOffDisplay() {
        return cardIdsForSwitchingOffDisplay;
    }

    public String getCommandForSwitchingOffDisplay() {
        return commandForSwitchingOffDisplay;
    }

    public void addInfoToInfoDisplay(String key, String value) {
        infoDisplay.put(key, value);
    }

    public boolean isShowingLog() {
        return showingLog;
    }

    public void setShowingLog(boolean showingLog) {
        this.showingLog = showingLog;
    }

    /* setters */
    public void setPanelDescription(int panelColumns, int panelRows, int moduleWidth, int moduleHeight, int widthInModules, int heightInModules) {
        /* para que 'Loader' pueda pasar a 'Configuration' la descripción del panel leida del xml */
        this.panelColumns = panelColumns;
        this.panelRows = panelRows;
        this.moduleWidth = moduleWidth;
        this.moduleHeight = moduleHeight;
        this.widthInModules = widthInModules;
        this.heightInModules = heightInModules;
    }
}
