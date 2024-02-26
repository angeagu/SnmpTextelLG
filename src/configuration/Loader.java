/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration;

import static launcher.LAUNCHER.VERSION;

import configuration.data.CardIdentifier;
import configuration.data.CheckerData;
import configuration.data.Command;
import configuration.data.CommandForHW;
import configuration.data.CommandForMEMORY;
import configuration.data.CommandForXML;
import configuration.data.DirectHWCommand;
import configuration.data.HardwareElementIdentifier;
import configuration.data.HardwareNotificationData;
import configuration.data.JFrameCommand;
import configuration.data.LeafNodeValueHWCommand;
import configuration.data.QueryData;
import configuration.data.QueryIdentifier;
import hardwareLayer.processors.AbstractMessageProcessor;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import snmpAgent.Velo;
import snmpAgent.data.GUIInfoUpdater;
import snmpAgent.data.SoftwareNotificationCondition;
import snmpAgent.data.AdminTextManager;
import snmpAgent.data.ConditionChecker;
import snmpAgent.data.SoftwareSnmpSet;

/**
 *
 * @author Tecnovision
 */
public class Loader {

    /* esta Clase sigue el patrón Singleton (una única instancia),
     el que se utiliza cuando una serie de datos han de estar disponibles
     para todos los demás objetos de la aplicación, o cuando una Clase controla
     el acceso a un recurso físico único, como un ratón, un puerto serie,
     un fichero abierto en modo exclusivo, etc.
     - variable 'instance' estática y privada
     - constructor privado
     - método 'getInstance' estático y público */
    private static Loader instance = null;
    private static final Logger log = Logger.getLogger(Loader.class.getName());

    // todas las constantes declaradas al principio, para luego no tener que recorrer
    // todo el código para cambiar cualquier texto
    public final static String ADMINISTRATOR_IP_ADDRESS_FOR_SNMP_NOTIFICATIONS = "ADMINISTRATOR_IP_ADDRESS";
    public final static String ADMINISTRATOR_PORT_FOR_SNMP_NOTIFICATIONS = "ADMINISTRATOR_PORT";
    public final static String ADMINISTRATOR_COMMUNITY_READ_ONLY_FOR_NOTIFICATIONS = "ADMINISTRATOR_READ_ONLY_COMMUNITY";
    public final static String ADMINISTRATOR_COMMUNITY_READ_WRITE_FOR_NOTIFICATIONS = "ADMINISTRATOR_READ_WRITE_COMMUNITY";

    /* tags para descripción del panel */
    public final static String PANEL = "PANEL";
    public final static String PANEL_COLUMNS = "COLUMNS";
    public final static String PANEL_ROWS = "ROWS";
    public final static String MODULE_WIDTH = "MODULE_WIDTH";
    public final static String MODULE_HEIGHT = "MODULE_HEIGHT";

    /* tags para Queries For Current Card */
    public final static String QUERY = "QUERY";
    public final static String ID = "ID";
    public final static String PROTOCOL = "PROTOCOL";
    public final static String OIDS = "OIDS";
    public final static String LOG_TRACE = "LOG";
    public final static String CARD_RESPONSE_FORMAT = "CARD_RESPONSE_FORMAT";
    public final static String PERIOD = "PERIOD";
    public final static String ON_START = "ON_START";
    public final static String DELAY_ON_START = "DELAY_ON_START";
    public final static String SEPARATOR = ",";
    public final static String ON_START_ENABLED = "1";
    public final static int ON_START_DISABLED = -1;
    public final static int LOG_DISABLED = 0;
    public final static int PERIOD_DISABLED_FOR_ON_START_QUERIES = -1;

    /* tags para Scheduled Queries For Current Card */
    public final static String SCHEDULED_QUERY = "SCHEDULED-QUERY";
    public final static String AT_SCHEDULED_TIME = "AT_SCHEDULED_TIME";
    public final static String ELEMENT_IDS = "ELEMENT_IDS";
    public final static int ONCE_A_DAY = 86400;

    /* tags para Software Queries For Current Card */
    public final static String SW_QUERY = "SW-QUERY";

    /* tags para Hardware Notifications For Current Card */
    public final static String HW_NOTIFICATION = "HW-NOTIFICATION";
    public final static String NOTIFICATION_FORMAT = "NOTIFICATION_FORMAT";
    public final static String OID = "OID";
    public final static String MESSAGE = "MESSAGE";
    
    /* tasg para Software Snmo Set */
    public final static String SW_SNMP_SET = "SW-SNMP-SET";
    public final static String BOUND_VALUE = "BOUND";
    public final static String ABOVE = "ABOVE";
    public final static String BELOW = "BELOW";

    /* tags para DIRECT_COMMAND_ON_SNMP_SET */
    public final static String DIRECT_COMMAND_ON_SNMP_SET = "DIRECT_COMMAND_ON_SNMP_SET";
    public final static String DIRECT_COMMAND_RESPONSE = "DIRECT_COMMAND_RESPONSE";
    public final static String DIRECT_COMMAND = "DIRECT_COMMAND";

    /* tags para Software Notifications For Current Card */
    public final static String SW_NOTIFICATION = "SW-NOTIFICATION";
    public final static String NOTIFICATION_OID = "NOTIFICATION_OID";
    public final static String CONDITION = "CONDITION";
    public final static String ELEMENT = "ELEMENT";
    public final static String LOWER_BOUND = "LOWER_BOUND";
    public final static String UPPER_BOUND = "UPPER_BOUND";
    public final static String STRING_VALUE_ERROR_CONDITION = "STRING_VALUE_ERROR_CONDITION";
    public final static String VALUE_MUST_MATCH_TO = "VALUE_MUST_MATCH_TO";
    public final static String BOUND = "BOUNDS";
    public final static String RETRIES = "RETRIES";

    public final static String VALUE = "VALUE";
    public final static String INTEGER = "Integer";
    public final static String STRING = "STRING";
    public final static String OPTION1 = "OPTION1";
    public final static String OPTION2 = "OPTION2";
    public final static String COMPONENT_TYPE = "COMPONENT_TYPE";
    public final static String SUFIX = "SUFIX";
    public final static String FACTOR = "FACTOR";
    public final static String DATA_TYPE = "DATA_TYPE";
    public final static String COMPONENT_IN_JFRAME = "COMPONENT_IN_JFRAME";
    public final static String JFRAME = "JFRAME";
    public final static String FACE = "FACE";
    public final static String CARD = "CARD";
    public final static String CHECKER = "CHECKER";
    public final static String UPDATER = "UPDATER";
    public final static String UPDATE = "UPDATE";
    public final static String CHECK = "CHECK";
    public final static String DELAY = "DELAY";
    public final static String ON_START_COMMAND = "ON-START-COMMAND";
    public final static String LEAF_NODE_VALUE = "LEAF_NODE_VALUE";
    public final static String COMMAND = "COMMAND";
    public final static String COMMAND_ON_SNMP_SET = "COMMAND_ON_SNMP_SET";
    public final static String OK = "OK";
    public final static String KO = "KO";

    public final static String DRIVE = "DRIVE";
    public final static String BEHAVIOUR_ON_USB_ERROR = "BEHAVIOUR_ON_USB_ERROR";
    public final static String SECONDS_TO_RESTART_APP = "SECONDS_TO_RESTART_APP";
    public final static String ATTEMPTS_TO_RESTART_APP_BEFORE_RESTART_PC = "ATTEMPTS_TO_RESTART_APP_BEFORE_RESTART_PC";
    public final static String MAX_ATTEMPTS_TO_RESTART_PC_BY_CARD = "MAX_ATTEMPTS_TO_RESTART_PC_BY_CARD";

    public final static String SWITCH_OFF_DISPLAY = "SWITCH_OFF_DISPLAY";
    public final static String SNMP_OID_VERSION = "SNMP_OID_VERSION";
    public final static String COMMAND_AUDIO_TEST = "COMMAND_AUDIO_TEST";
    public final static String COMMAND_VIDEO_TEST = "COMMAND_VIDEO_TEST";
    public final static String CARD_IDS = "CARD_IDS";

    public final static String NOTIFICATIONS = "NOTIFICATIONS";
    public final static String PARAMETER = "PARAMETER";

    public final static String DIRECT_TEXT = "DIRECT_TEXT";

    /* tags for IgnoreSerialPortNames */
    public final static String IGNORE_SERIAL_PORTS = "IGNORE_SERIAL_PORTS";
    public final static String NAMES = "NAMES";

    /* tags for Velo */
    public final static String VEIL = "VEIL";
    public final static String EXTERNAL_VEIL_CARDS = "EXTERNAL_VEIL_CARD";
    public final static String EXTERNAL_VEIL_COMMAND = "EXTERNAL_VEIL_CMD";
    public final static String EXTERNAL_VEIL_RESPONSE = "EXTERNAL_VEIL_RESPONSE";
    public final static String CONVERSION_FACTOR = "CONVERSION_FACTOR";
    public final static String HEIGHT = "HEIGHT";
    public final static String WIDTH = "WIDTH";
    public final static String POS_X = "POS_X";
    public final static String POS_Y = "POS_Y";
    public final static String MINIMUM_BRIGHTNESS = "MINIMUM_BRIGHTNESS";

    /* tags for JFrameCommand */
    public final static String COMMAND_BUTTON = "COMMAND_BUTTON";
    public final static String CARD_ID = "CARD_ID";
    public final static String FACE_ID = "FACE_ID";

    /* tags for Query for card id */
    public final static String QUERY_FOR_CARD_ID = "QUERY_FOR_CARD_ID";

    /* tags for brightness profiles */
    public final static String BRIGHTNESS_PROFILES = "BRIGHTNESS_PROFILES";
    public final static String X_POS_REF = "X_POS_REF";
    public final static String Y_POS_REF = "Y_POS_REF";
    public final static String PROFILE = "PROFILE";

    /* tags for Software Commands (SNMP set) */
    public final static String SW_COMMAND_ON_SNMP_SET = "SW_COMMAND_ON_SNMP_SET";
    public final static String DATA = "DATA";
    public final static String DESTINATION = "DESTINATION";

    /* tags for Admin Text Manager */
    public final static String TEXT_MANAGER = "TEXT_MANAGER";
    public final static String MAIN_OIDS = "MAIN_OIDS";
    public final static String TEXT_PARAMETER = "TEXT_PARAMETER";
    public final static String TYPE = "TYPE";
    public final static String TEXT_HEIGHT_WHEN_1_LINE = "TEXT_HEIGHT_WHEN_1_LINE";
    public final static String TEXT_HEIGHT_WHEN_2_LINES = "TEXT_HEIGHT_WHEN_2_LINES";

    // éste el el método de entrada a la Clase
    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
        }

        return instance;
    }

    public void readXmlConfiguration(String xmlFilename) {
        /* leer un archivo 'xml' con la configuración del panel
         y dejarla en el HashMap 'infoDisplay' para cuando
         alguien la pida (con 'getInfoDisplay') */

        try {
            /* se parsea el documento xml */
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(xmlFilename);
            Element doc = dom.getDocumentElement();

            /* leemos la descripción del panel */
            this.readPanelDescription(doc);

            /* recorremos todas las caras
             y todas las tarjetas de cada cara
             y todas las queries, notifications y commands de cada tarjeta*/
            NodeList faces = doc.getElementsByTagName(FACE);
            for (int faceIndex = 0; faceIndex < faces.getLength(); faceIndex++) {
                Element currentFace = (Element) faces.item(faceIndex);
                String faceId = currentFace.getAttribute(ID).trim();
                NodeList cards = currentFace.getElementsByTagName(CARD);

                /* recorremos todas tarjetas de la cara actual */
                for (int cardIndex = 0; cardIndex < cards.getLength(); cardIndex++) {
                    Element currentCard = (Element) cards.item(cardIndex);
                    String cardId = currentCard.getAttribute(ID).trim();
                    String protocolOfCardId = currentCard.getAttribute(PROTOCOL).trim();
                    String queryForCardId = currentCard.getAttribute(QUERY_FOR_CARD_ID).trim();

                    /* como sabemos FaceId y CardId,
                     registramos la tarjeta actual para la cara actual
                     añadiéndola al TreeSet cardIdsByFaceId que, como su nombre indica,
                     guarda el nombre de todas las tarjetas que tiene cada cara.
                     TreeSet es como un ArrayList, pero no permite claves repetidas */
                    Configuration.getInstance().addCardIdToFaceId(faceId, cardId);

                    Configuration.getInstance().addProtocolByCardId(cardId, protocolOfCardId);////// comentar!
                    
                    Configuration.getInstance().addQueryForCardId(protocolOfCardId, queryForCardId);////// comentar!

                    /* leemos todos los onStartCommands */
                    this.readOnStartDelayedCommandsForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todas las QUERY de la tarjeta actual */
                    this.readQueriesForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todas las SCHEDULED-QUERY de la tarjeta actual */
                    this.readScheduledQueriesForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todas las SW-QUERY de la tarjeta actual */
                    this.readSoftwareQueriesForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todas las HW-NOTIFICATION */
                    this.readHardwareNotificationForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todas las SW-NOTIFICATION */
                    this.readSoftwareNotificationForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todos los comandos que se pueden ejecutar al recibir un SET */
                    this.readCommandsOnSnmpSetForCurrentCard(faceId, cardId, currentCard);
                    
                    /* Leemos todos los comandos que se pueden ejecutar al recibir un SET */
                    this.readDirectCommandsOnSnmpSetForCurrentCard(faceId, cardId, currentCard);

                    /* Leemos todos los software snmp set */
                    this.readSoftwareSnmpSetForCurrentCard(currentCard);
                }
            }

            /* leemos los Checkers */
            this.readCheckers(doc);

            /* leemos los JFrame Updaters */
            this.readJFrameUdpaters(doc);

            /* leemos los jframe commands */
            this.readJFrameCommands(doc);

            /* leemos los perfiles de brillo y los velos */
            this.readVeloInfo(doc);

            /* leemos el comportamiento ante errores en el USB */
            this.readBehaviourOnUsbError(doc);

            /* leemos info para apagar todos los displays */
            this.readInfoForSwitchingOffDisplay(doc);

            /* leemos los parámetros de notificaciones SNMP */
            this.readSnmpNotificationParameters(doc);

            /* leemos los parámetros de notificaciones TRAP */
            this.readSwCommandsForSnmpNotificationParameters(doc);   //////

            /* leemos los parámetros de textoDirecto */
            this.readDirectTextParameters(doc);

            /* leemos los comandos de xml de textoDirecto */
            this.readSwCommandsForDirectTextParameters(doc);

            /* leemos los comandos de memoria de textoDirecto */
            this.readCommandsForMemoryUpdate(doc);

            /* leemos los managers de los textos mandados por el admin */
            this.readAdminTextManagers(doc);

            /* Leemos el OID para la versión actual del software (Snmp) */
            this.readInfoSnmpVersionOid(doc);

            /* Leemos el comando que se va a ejecutar cuando se realice el test de audio y video */
            this.readInfoRunAudioTest(doc);
            this.readInfoRunVideoTest(doc);

            /* Leemos los nombres de los puertos serie que se deben eliminar */
            this.readIgnoredSerialPorts(doc);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            log.error(ex);
        }
    }

    private void readPanelDescription(Element doc) {
        int panelRows;
        int panelColumns;
        int moduleWidth;
        int moduleHeight;
        int widthInModules;
        int heightInModules;

        /* queremos recorrer los atributos del elemento 'panel' */
        NodeList panelDescriptionNode = doc.getElementsByTagName(PANEL);

        /* por definición, sólo tenemos un elemento */
        if (panelDescriptionNode.getLength() == 1) {
            Element panelDescription = (Element) panelDescriptionNode.item(0);

            try {
                panelColumns = Integer.parseInt(panelDescription.getAttribute(PANEL_COLUMNS).trim());
                panelRows = Integer.parseInt(panelDescription.getAttribute(PANEL_ROWS).trim());
                moduleWidth = Integer.parseInt(panelDescription.getAttribute(MODULE_WIDTH).trim());
                moduleHeight = Integer.parseInt(panelDescription.getAttribute(MODULE_HEIGHT).trim());
                widthInModules = panelColumns / moduleWidth;
                heightInModules = panelRows / moduleHeight;

            } catch (NumberFormatException ex) {
                panelColumns = 192;
                panelRows = 48;
                moduleWidth = 32;
                moduleHeight = 16;
                widthInModules = 6;
                heightInModules = 3;
                log.error("Unexpected panel description. We assume following default values: "
                        + "PANEL COLUMNS [192] PANEL ROWS [48] MODULE WIDTH [32] MODULE HEIGHT [16]");
            }

            /* pasamos a 'Configuration' los parámetros leídos, para cuando los podamos necesitar */
            Configuration.getInstance().setPanelDescription(panelColumns, panelRows, moduleWidth, moduleHeight, widthInModules, heightInModules);

        } else {
            log.error("Unexpected panel description");
        }
    }

    /* en éste y en los sucesivos métodos, cardId es el nombre de la tarjeta,
     y se ha obtenido previamente del propio elemento currentCard (la lectura del xml)
     que también se recibe como argumento. Aunque es redundante, así queda más pulido */
    private void readOnStartDelayedCommandsForCurrentCard(String faceId, String cardId, Element currentCard) {

        /* buscamos en el elemento currentCard (cada una de las sucesivas lecturas del elemento 'tarjeta' en el xml)
         para obtener la lista de ON-START-COMMAND que tenga definidos */
        NodeList onStartDelayedCommandsOfCurrentCard = currentCard.getElementsByTagName(ON_START_COMMAND);

        /* ahora recorremos todos los ON-START-COMMANDs de la lista
         para obtener la ID y el DELAY de cada comando */
        for (int onStartDelayedCommandIndex = 0; onStartDelayedCommandIndex < onStartDelayedCommandsOfCurrentCard.getLength(); onStartDelayedCommandIndex++) {

            boolean errorOnCurrentCommand;   // booleano para saber si procesamos el comando o no
            int integerDelayOnStart = -2;    // variable entera en la que almacenamos el retardo

            /* nos vamos quedando con cada uno de los elementos ON-START-COMMAND de la lista */
            Element currentOnStartDelayedCommandNode = (Element) onStartDelayedCommandsOfCurrentCard.item(onStartDelayedCommandIndex);

            /* leemos todos los atributos (ID y DELAY)que esperamos encontrar en ese elemento */
            String command = currentOnStartDelayedCommandNode.getAttribute(ID).trim();
            String delayOnStart = currentOnStartDelayedCommandNode.getAttribute(DELAY).trim();

            if (command.length() > 0) {
                try {
                    integerDelayOnStart = Integer.parseInt(delayOnStart);
                    errorOnCurrentCommand = false;

                } catch (NumberFormatException ex) {
                    errorOnCurrentCommand = true;
                    log.error("Discarding wrong onStart command [" + command + "] "
                            + "with delayOnStart [" + delayOnStart + "]");
                }

            } else {
                errorOnCurrentCommand = true;
                log.error("Unexpected onStart command [" + command + "] in faceId [" + faceId + "] cardId [" + cardId + "]");
            }

            /* si no hay errores en el command, se procesa */
            if (!errorOnCurrentCommand) {

                /* damos de alta el delayed command como una onStartQuery
                 a la que le deshabilitamos el periodo */
                QueryData commandData = new QueryData(command);
                CardIdentifier cardIdentifier = new CardIdentifier(cardId, faceId);
                Configuration.getInstance().addScheduledQuery(
                        cardIdentifier,
                        PERIOD_DISABLED_FOR_ON_START_QUERIES,
                        integerDelayOnStart,
                        commandData);
            }
        }
    }

    private void readQueriesForCurrentCard(String faceId, String cardId, Element currentCard) {

        /* buscamos en el elemento currentCard (cada una de las sucesivas lecturas del elemento 'tarjeta' en el xml)
         para obtener la lista de QUERYs que tenga definidos */
        NodeList queriesOfCurrentCard = currentCard.getElementsByTagName(QUERY);

        /* recorremos todas las queries de la tarjeta actual */
        for (int queryIndex = 0; queryIndex < queriesOfCurrentCard.getLength(); queryIndex++) {

            boolean errorOnCurrentQuery;
            int integerPeriod = -2;
            int integerDelayOnStart = -1;
            /////
            int integerLogTrace = 0;   // v.1.2.2

            Element currentQueryNode = (Element) queriesOfCurrentCard.item(queryIndex);

            /* leyendo todos los atributos del query */
            String query = currentQueryNode.getAttribute(ID).trim();
            String formatResponse = currentQueryNode.getAttribute(CARD_RESPONSE_FORMAT).trim();
            String period = currentQueryNode.getAttribute(PERIOD).trim();
            String onStart = currentQueryNode.getAttribute(ON_START).trim();
            String oids = currentQueryNode.getAttribute(OIDS).trim();
            /////
            String logTrace = currentQueryNode.getAttribute(LOG_TRACE).trim();   // V.1.2.2

            /* extraemos ciertos valores que vienen en campos especiales,
             como los identificadores de la respuesta (Dx, Pa, Tsxxx, V1xV2xV3xV4x)
             y los OIDs que van a ser actualizados con ellos */
            /* con StringTokenizer podemos declarar */
            StringTokenizer responseIds = new StringTokenizer(formatResponse, AbstractMessageProcessor.FORMAT_RESPONSE_DELIMETERS);
            String[] stringOids = oids.split(SEPARATOR);

            /* analizamos si hay errores en el query */
            if (responseIds.countTokens() == stringOids.length) {
                try {
                    integerPeriod = Integer.parseInt(period);
                    integerDelayOnStart = Integer.parseInt(onStart);
                    errorOnCurrentQuery = false;
                    /////
                    integerLogTrace = Integer.parseInt(logTrace);   // v.1.2.2

                } catch (NumberFormatException ex) {
                    errorOnCurrentQuery = true;
                    log.error("Discarding wrong query [" + query + "] "
                            + "with period [" + period + "] "
                            + "onStart [" + onStart + "] "
                            /////
                            + "and logTrace [" + logTrace + "].");
                }

            } else {
                errorOnCurrentQuery = true;
                log.error("Error: response format [" + formatResponse + "] "
                        + "doesn't match the number of OIDs found in query [" + query + "]. "
                        + "Discarding query.");
            }

            /* si no hay errores en el query, se procesa */
            if (!errorOnCurrentQuery) {
                ArrayList<String> oidList = new ArrayList<>();

                /* vamos a almacenar el formato de respuesta
                 asociandolo con la clave Cara-Tarjeta-PrimeraLetra */
                //String firstCharOfQuery = currentQueryNode.getAttribute(ID).trim().substring(0, 1); //////para textel deberia ser igual, ya que la primera letra de la query es la misma que de la respuesta. En LG no pasa eso, por eso vamos a usar la primera letra de la respuesta.
                String firstCharOfQuery = formatResponse.trim().substring(0, 1); ///////cambiar para todos los tipos de queries!!!!
                QueryIdentifier queryIdentifier = new QueryIdentifier(faceId, cardId, firstCharOfQuery);
                Configuration.getInstance().addQueryResponseFormat(queryIdentifier, formatResponse);

                /* asociamos cada OID con su clave identificadorElementoHardware
                 para cuando recibamos la respuesta del query */
                for (String stringOid : stringOids) {
                    HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, responseIds.nextToken());
                    Configuration.getInstance().addOIDForQueries(hwElementId, stringOid);
                    oidList.add(stringOid);
                }

                /* vamos a programar la query */
                CardIdentifier cardIdentifier = new CardIdentifier(cardId, faceId);

                /* encapsulamos el string en una clase */
                QueryData queryData = new QueryData(query);
                QueryData queryDataForLog = new QueryData(query, oidList);

                /* añadimos la scheduled query con periodo
                 Al principio, period y secondsToSendPeriod valen lo mismo*/
                Configuration.getInstance().addScheduledQuery(cardIdentifier, integerPeriod, integerPeriod, queryData);

                /* añadimos la misma scheduled query con delayed onStart (siempre que no sea -1) */
                if (integerDelayOnStart != ON_START_DISABLED) {
                    Configuration.getInstance().addScheduledQuery(
                            cardIdentifier,
                            PERIOD_DISABLED_FOR_ON_START_QUERIES,
                            integerDelayOnStart,
                            queryData);
                }

                ////
                if (integerLogTrace != LOG_DISABLED) {
                    Configuration.getInstance().addScheduledQuery(
                            cardIdentifier,
                            integerLogTrace,
                            integerLogTrace,
                            queryDataForLog);
                }
            }
        }
    }

    private void readScheduledQueriesForCurrentCard(String faceId, String cardId, Element currentCard) {

        /* buscamos en el elemento currentCard (cada una de las sucesivas lecturas del elemento 'tarjeta' en el xml)
         para obtener la lista de SCHEDULED_QUERYs que tenga definidos */
        NodeList schQueriesOfCurrentCard = currentCard.getElementsByTagName(SCHEDULED_QUERY);

        /* recorremos todas las scheduledQueries de la tarjeta actual */
        for (int schQueryIndex = 0; schQueryIndex < schQueriesOfCurrentCard.getLength(); schQueryIndex++) {

            boolean errorOnCurrentQuery;
            int timeToExecuteScheduledQuery = -2;
            int integerDelayOnStart = -2;

            Element currentSchQueryNode = (Element) schQueriesOfCurrentCard.item(schQueryIndex);

            /* Leemos todos los atributos del query */
            String query = currentSchQueryNode.getAttribute(ID).trim();
            String oids = currentSchQueryNode.getAttribute(OIDS).trim();
            String formatResponse = currentSchQueryNode.getAttribute(CARD_RESPONSE_FORMAT).trim();
            String onStart = currentSchQueryNode.getAttribute(ON_START).trim();
            String atScheduledTime = currentSchQueryNode.getAttribute(AT_SCHEDULED_TIME).trim();
            String elementIds = currentSchQueryNode.getAttribute(ELEMENT_IDS).trim();

            /* extraemos ciertos valores que vienen en campos especiales,
             como los identificadores de los elementos consultados (wrongLeds, wrongModules)
             y los OIDs que van a ser actualizados con ellos */
            String[] stringElementIds = elementIds.split(SEPARATOR);
            String[] stringOids = oids.split(SEPARATOR);

            /* analizamos si hay errores en el scheduled query */
            if (stringElementIds.length == stringOids.length) {
                try {
                    timeToExecuteScheduledQuery = this.computeTimeToExecuteScheduledQuery(atScheduledTime, query);
                    integerDelayOnStart = Integer.parseInt(onStart);
                    errorOnCurrentQuery = false;

                } catch (NumberFormatException ex) {
                    errorOnCurrentQuery = true;
                    log.error("Error: wrong scheduled query "
                            + "at time [" + atScheduledTime + "] onStart [" + onStart + "] "
                            + "on query [" + query + "]. Discarding scheduled query.");
                }

            } else {
                errorOnCurrentQuery = true;
                log.error("Error: the number of element IDs doesn't match "
                        + "the number of OIDs found in scheduled query [" + query + "]. "
                        + "Discarding scheduled query.");
            }

            /* Si no hay errores en el query, se procesa */
            if (!errorOnCurrentQuery) {

                /* almacenamos el formato de respuesta */
                String firstCharOfQuery = currentSchQueryNode.getAttribute(ID).trim().substring(0, 1);
                QueryIdentifier queryIdentifier = new QueryIdentifier(faceId, cardId, firstCharOfQuery);
                Configuration.getInstance().addQueryResponseFormat(queryIdentifier, formatResponse);

                /* mapeamos cada OID con el identificador proporcionado por la respuesta del query */
                for (int oidIndex = 0; oidIndex < stringOids.length; oidIndex++) {
                    HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, stringElementIds[oidIndex]);
                    Configuration.getInstance().addOIDForQueries(hwElementId, stringOids[oidIndex]);
                }

                /* programamos la query */
                CardIdentifier cardIdentifier = new CardIdentifier(cardId, faceId);

                /* encapsulamos el string en una clase */
                QueryData queryData = new QueryData(query);

                /* añadimos la scheduled query con periodo */
                Configuration.getInstance().addScheduledQuery(cardIdentifier, ONCE_A_DAY, timeToExecuteScheduledQuery, queryData);

                /* añadimos la scheduled query con onStart (siempre que no sea -1) */
                if (integerDelayOnStart != ON_START_DISABLED) {
                    Configuration.getInstance().addScheduledQuery(cardIdentifier, PERIOD_DISABLED_FOR_ON_START_QUERIES,
                            integerDelayOnStart, queryData);
                }
            }
        }
    }

    private void readSoftwareQueriesForCurrentCard(String faceId, String cardId, Element currentCard) {

        /* buscamos en el elemento currentCard (cada una de las sucesivas lecturas del elemento 'tarjeta' en el xml)
         para obtener la lista de SOFTWARE_QUERYs que tenga definidos */
        NodeList swQueriesOfCurrentCard = currentCard.getElementsByTagName(SW_QUERY);

        /* recorremos todas las swQueries de la tarjeta actual */
        for (int swQueryIndex = 0; swQueryIndex < swQueriesOfCurrentCard.getLength(); swQueryIndex++) {

            /* leemos todos los atributos del swQuery */
            Element currentSwQueryNode = (Element) swQueriesOfCurrentCard.item(swQueryIndex);
            String elementIds = currentSwQueryNode.getAttribute(ELEMENT_IDS).trim();
            String oids = currentSwQueryNode.getAttribute(OIDS).trim();

            /* troceamos los ids y oids */
            String[] stringElementIds = elementIds.split(SEPARATOR);
            String[] stringOids = oids.split(SEPARATOR);

            /* analizamos si hay errores en el query */
            if (stringElementIds.length == stringOids.length) {

                /* insertamos los valores */
                for (int oidIndex = 0; oidIndex < stringOids.length; oidIndex++) {
                    HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, stringElementIds[oidIndex]);
                    Configuration.getInstance().addOIDForQueries(hwElementId, stringOids[oidIndex]);
                }

            } else {
                log.error("Error: the number of element IDs [" + elementIds + "] doesn't match "
                        + "the number of OIDs [" + oids + "] found in software query."
                        + "Discarding software query.");
            }
        }
    }

    private void readHardwareNotificationForCurrentCard(String faceId, String cardId, Element currentCard) {
        NodeList notificationsOfCurrentCard = currentCard.getElementsByTagName(HW_NOTIFICATION);

        for (int notificationIndex = 0; notificationIndex < notificationsOfCurrentCard.getLength(); notificationIndex++) {
            Element currentNotificationNode = (Element) notificationsOfCurrentCard.item(notificationIndex);

            /* Leemos todos los atributos del xml */
            String notificationFormat = currentNotificationNode.getAttribute(NOTIFICATION_FORMAT).trim();
            String oid = currentNotificationNode.getAttribute(OID).trim();
            String notificationMessage = currentNotificationNode.getAttribute(MESSAGE).trim();

            if ((notificationFormat.length() > 0) && (oid.length() > 0) && (notificationMessage.length() > 0)) {
                String elementId = notificationFormat.substring(0, 1);

                /* Guardamos el formato de la notificaion */
                QueryIdentifier queryIdentifier = new QueryIdentifier(faceId, cardId, elementId);
                HardwareNotificationData data = new HardwareNotificationData(notificationFormat, notificationMessage);
                Configuration.getInstance().addNotificationFormat(queryIdentifier, data);

                /* Mapeamos el OID con el identificador de la notificaion hardware */
                HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, elementId);
                Configuration.getInstance().addOIDForNotification(hwElementId, oid);

            } else {
                log.error("Error: some of the HW notification fields are empty. "
                        + "format [" + notificationFormat + "] oid [" + oid + "] message [" + notificationMessage + "]. "
                        + "Discarding Hardware Notification.");
            }
        }
    }

    private void readSoftwareNotificationForCurrentCard(String faceId, String cardId, Element currentCard) {
        NodeList swNotificationsOfCurrentCard = currentCard.getElementsByTagName(SW_NOTIFICATION);

        for (int swNotificationIndex = 0; swNotificationIndex < swNotificationsOfCurrentCard.getLength(); swNotificationIndex++) {
            int intRetries;
            boolean errorOnNotification;
            SoftwareNotificationCondition softwareNotificationCondition = null;
            Element currentSwNotificationNode = (Element) swNotificationsOfCurrentCard.item(swNotificationIndex);

            /* Leemos todos los atributos del xml */
            String oid = currentSwNotificationNode.getAttribute(OID).trim();
            String notificationOid = currentSwNotificationNode.getAttribute(NOTIFICATION_OID).trim();
            String condition = currentSwNotificationNode.getAttribute(CONDITION).trim();
            String element = currentSwNotificationNode.getAttribute(ELEMENT).trim();
            String lowerBound = currentSwNotificationNode.getAttribute(LOWER_BOUND).trim();
            String upperBound = currentSwNotificationNode.getAttribute(UPPER_BOUND).trim();
            String stringValueError = currentSwNotificationNode.getAttribute(STRING_VALUE_ERROR_CONDITION).trim();
            String matchToValueOfOidElements = currentSwNotificationNode.getAttribute(VALUE_MUST_MATCH_TO).trim();
            String retries = currentSwNotificationNode.getAttribute(RETRIES).trim();

            /* Procesamos el numero de reintentos, si no hay, se asume 1 */
            try {
                intRetries = Integer.parseInt(retries);

            } catch (NumberFormatException ex) {
                intRetries = 1;
            }

            /* Comprobamos que los datos sean correctos */
            if ((oid.length() > 0) && (notificationOid.length() > 0) && (condition.length() > 0) && (element.length() > 0)) {

                switch (condition) {
                    case BOUND:
                        if ((lowerBound.length() > 0) && (upperBound.length() > 0)) {
                            softwareNotificationCondition = new SoftwareNotificationCondition(lowerBound,
                                    upperBound,
                                    notificationOid,
                                    intRetries,
                                    element);
                            errorOnNotification = false;

                        } else {
                            errorOnNotification = true;
                            log.error("Error: notification with OID [" + oid + "] and BOUNDS condition "
                                    + "has undefined upperBound [" + upperBound + "] and/or lowerBound [" + lowerBound + "]. "
                                    + "Discarding SW Notification.");
                        }

                        break;

                    case STRING_VALUE_ERROR_CONDITION:
                        softwareNotificationCondition = new SoftwareNotificationCondition(stringValueError,
                                notificationOid, intRetries, element);
                        errorOnNotification = false;
                        break;

                    case VALUE_MUST_MATCH_TO:
                        if (matchToValueOfOidElements.length() > 0) {
                            softwareNotificationCondition = new SoftwareNotificationCondition(matchToValueOfOidElements.split(SEPARATOR),
                                    notificationOid, intRetries, element);
                            errorOnNotification = false;

                        } else {
                            errorOnNotification = true;
                            log.error("Error: notification with OID [" + oid + "] and VALUE_MUST_MATCH_TO condition "
                                    + "has undefined 'id' in VALUE_MUST_MATCH_TO attribute. "
                                    + "Discarding SW Notification.");
                        }
                        break;

                    default:
                        errorOnNotification = true;
                        log.error("Se descarta la notificacion software porque el tipo"
                                + " de condicion indicado [" + condition + "] es erróneo.");
                }

            } else {
                errorOnNotification = true;
                log.error("Se descarta la notificacion software porque el oid [" + oid
                        + "], notificationOid [" + notificationOid + "], condition ["
                        + condition + "] o element [" + element + "] no han sido definidos.");
            }


            /* Si no hay errores se procesa */
            if (!errorOnNotification) {

                /* Mapeamos cada OID con el identificador proporcionado por la respuesta del query */
                HardwareElementIdentifier hwElementId = new HardwareElementIdentifier(faceId, cardId, element);
                Configuration.getInstance().addOIDForNotification(hwElementId, notificationOid);

                /* Almacenamos la conficion de notificacion software */
                Configuration.getInstance().addSoftwareNotification(oid, softwareNotificationCondition);
            }
        }
    }

    private void readCommandsOnSnmpSetForCurrentCard(String faceId, String cardId, Element currentCard) {
        NodeList commandsOfCurrentCard = currentCard.getElementsByTagName(COMMAND_ON_SNMP_SET);

        for (int commandIndex = 0; commandIndex < commandsOfCurrentCard.getLength(); commandIndex++) {
            LeafNodeValueHWCommand currentCommand;
            Element currentCommandNode = (Element) commandsOfCurrentCard.item(commandIndex);

            /* Leemos todos los atributos del xml */
            String oid = currentCommandNode.getAttribute(OID).trim();
            NodeList commandOfCurrentCommandNode = currentCommandNode.getElementsByTagName(COMMAND);

            if ((oid.length() > 0) && (commandOfCurrentCommandNode.getLength() > 0)) {
                currentCommand = new LeafNodeValueHWCommand(faceId, cardId, Command.HARDWARE);

                for (int commandValueIndex = 0; commandValueIndex < commandOfCurrentCommandNode.getLength(); commandValueIndex++) {
                    Element currentCommandOfCurrentCommandNode = (Element) commandOfCurrentCommandNode.item(commandValueIndex);
                    String value = currentCommandOfCurrentCommandNode.getAttribute(LEAF_NODE_VALUE).trim();
                    String id = currentCommandOfCurrentCommandNode.getAttribute(ID).trim();
                    String formatResponse = currentCommandOfCurrentCommandNode.getAttribute(CARD_RESPONSE_FORMAT).trim();

                    if ((value.length() > 0) && (id.length() > 0)) {
                        currentCommand.addCommand(value, id);
                        log.info("Se crea el comando " + id + " para el valor " + value);
                        
                        if(!formatResponse.isEmpty()){
                            String firstCharOfCommand = formatResponse.trim().substring(0, 1);
                            QueryIdentifier queryIdentifier = new QueryIdentifier(faceId, cardId, firstCharOfCommand);
                            Configuration.getInstance().addCommandResponseFormat(queryIdentifier, formatResponse);
                        }

                    } else {
                        log.error("Discarding command [" + id + "] with value ["
                                + value + "] for oid [" + oid + "]");
                    }
                }

                Configuration.getInstance().addCommand(oid, currentCommand);

            } else {
                log.error("Discarding command for oid [" + oid + "]");
            }
        }
    }

    private void readDirectCommandsOnSnmpSetForCurrentCard(String faceId, String cardId, Element currentCard) {
        NodeList commandsOfCurrentCard = currentCard.getElementsByTagName(DIRECT_COMMAND_ON_SNMP_SET);

        for (int commandIndex = 0; commandIndex < commandsOfCurrentCard.getLength(); commandIndex++) {
            CommandForHW currentCommand;
            Element currentCommandNode = (Element) commandsOfCurrentCard.item(commandIndex);

            /* Leemos todos los atributos del xml */
            String oid = currentCommandNode.getAttribute(OID).trim();
            String directCommand = currentCommandNode.getAttribute(DIRECT_COMMAND).trim();
            String directCommandResponse = currentCommandNode.getAttribute(DIRECT_COMMAND_RESPONSE).trim();

            if (oid.length() > 0 && directCommand.length() > 0 && directCommandResponse.length() > 0) {
                currentCommand = new DirectHWCommand(faceId, cardId, Command.HARDWARE, directCommand, directCommandResponse);
                Configuration.getInstance().addCommand(oid, currentCommand);
                log.info("Se crea el comando " + directCommand + " con ID response " + directCommandResponse);

            } else {
                log.error("Discarding direct command for oid [" + oid + "]");
            }
        }
    }

    private void readCheckers(Element doc) {
        NodeList checkers = doc.getElementsByTagName(CHECKER);

        /* Leemos todos los checkers */
        for (int checkerIndex = 0; checkerIndex < checkers.getLength(); checkerIndex++) {
            Element currentChecker = (Element) checkers.item(checkerIndex);
            String mainOid = currentChecker.getAttribute(OID).trim();
            String ok = currentChecker.getAttribute(OK).trim();
            String ko = currentChecker.getAttribute(KO).trim();

            /* El valor del mainOID es calculado en funcion de los siquientes oids */
            ArrayList<String> oidList = new ArrayList<>();
            NodeList checks = currentChecker.getElementsByTagName(CHECK);
            for (int checkIndex = 0; checkIndex < checks.getLength(); checkIndex++) {
                Element currentCheck = (Element) checks.item(checkIndex);
                String oid = currentCheck.getAttribute(OID).trim();
                oidList.add(oid);
            }

            CheckerData data = new CheckerData(ok, ko, oidList);
            Configuration.getInstance().addChecker(mainOid, data);
        }
    }

    private void readJFrameUdpaters(Element doc) {
        NodeList updaters = doc.getElementsByTagName(UPDATER);

        for (int updaterIndex = 0; updaterIndex < updaters.getLength(); updaterIndex++) {
            Element currentUpdater = (Element) updaters.item(updaterIndex);
            String jFrameId = currentUpdater.getAttribute(JFRAME).trim();

            /* Leemos todos los update del jFrameId actual */
            NodeList updates = currentUpdater.getElementsByTagName(UPDATE);
            for (int updateIndex = 0; updateIndex < updates.getLength(); updateIndex++) {
                boolean errorOnCurrentUpdate = false;

                /* Leemos todos los atributos del elemento en el xml */
                Element currentUpdate = (Element) updates.item(updateIndex);
                String oid = currentUpdate.getAttribute(OID).trim();
                String componentInJFrame = currentUpdate.getAttribute(COMPONENT_IN_JFRAME).trim();
                String dataType = currentUpdate.getAttribute(DATA_TYPE).trim();
                String sufix = currentUpdate.getAttribute(SUFIX).trim();
                String stringFactor = currentUpdate.getAttribute(FACTOR).trim();
                String componentType = currentUpdate.getAttribute(COMPONENT_TYPE).trim();
                String option1 = currentUpdate.getAttribute(OPTION1).trim();
                String option2 = currentUpdate.getAttribute(OPTION2).trim();
                double factor = 1;

                if (dataType.equalsIgnoreCase(INTEGER) || dataType.equalsIgnoreCase(STRING)
                        || dataType.equalsIgnoreCase(VEIL)) {

                    if ((oid.length() > 0) && (componentInJFrame.length() > 0) && (componentType.length() > 0)) {
                        try {
                            factor = Double.parseDouble(stringFactor);

                        } catch (NumberFormatException ex) {

                            if (!dataType.equalsIgnoreCase(STRING)) {
                                log.warn("Wrong factor [" + stringFactor + "] for OID UPDATE ["
                                        + oid + "] se asume 1.0");
                            }
                        }
                    } else {
                        errorOnCurrentUpdate = true;
                        log.error("Unexpected oid[ " + oid + "] componentInJFrame ["
                                + componentInJFrame + "] or componentType [" + componentType
                                + "]. We discard this OID UPDATE.");
                    }

                } else {
                    errorOnCurrentUpdate = true;
                    log.error("Unexpected data type [" + dataType + "] for oid UPDATE ["
                            + oid + "]. We discard this UPDATE.");
                }

                /* Si no hay error, proceso el update */
                if (!errorOnCurrentUpdate) {
                    GUIInfoUpdater currentGUIUpdater = new GUIInfoUpdater(sufix, factor,
                            componentInJFrame, dataType, componentType, option1, option2, oid);
                    Configuration.getInstance().addJFrameUpdater(jFrameId, oid, currentGUIUpdater);
                }
            }
        }
    }

    private void readVeloInfo(Element doc) {
        /* Antes leemos todos los perfiles del brillo */
        this.readBrightnessProfiles(doc);

        NodeList veils = doc.getElementsByTagName(VEIL);

        for (int veilIndex = 0; veilIndex < veils.getLength(); veilIndex++) {
            Element currentVeil = (Element) veils.item(veilIndex);

            /* Leemos todos los atributos del elemento en el xml */
            String stringConversionFactor = currentVeil.getAttribute(CONVERSION_FACTOR).trim();
            String stringHeight = currentVeil.getAttribute(HEIGHT).trim();
            String stringWidth = currentVeil.getAttribute(WIDTH).trim();
            String stringPosX = currentVeil.getAttribute(POS_X).trim();
            String stringPosY = currentVeil.getAttribute(POS_Y).trim();
            String stringMinimumBrightness = currentVeil.getAttribute(MINIMUM_BRIGHTNESS).trim();
            String oids = currentVeil.getAttribute(OIDS).trim();
            String externalVeilCards = currentVeil.getAttribute(EXTERNAL_VEIL_CARDS).trim();
            String externalVeilCommand = currentVeil.getAttribute(EXTERNAL_VEIL_COMMAND).trim();
            String externalVeilResponse = currentVeil.getAttribute(EXTERNAL_VEIL_RESPONSE).trim();
            String brightnessProfile = Configuration.getInstance().getInfoDisplay(Configuration.BRIGHTNESS_PROFILE).trim();

            try {

                Double conversionFactor = Double.parseDouble(stringConversionFactor);
                int height = Integer.parseInt(stringHeight);
                int width = Integer.parseInt(stringWidth);
                int posX = Integer.parseInt(stringPosX);
                int posY = Integer.parseInt(stringPosY);
                int minimumBrightness = Integer.parseInt(stringMinimumBrightness);

                if (oids.length() > 0) {
                    Point brigthtnessPoint = Configuration.getInstance().getBrightnessProfile(brightnessProfile);

                    if (brigthtnessPoint == null) {
                        log.error("Error on brightness profile id [" + brightnessProfile + "] on velo for oids ["
                                + oids + "]. We assume default point x = 50 y = 50.");
                        brigthtnessPoint = new Point(50, 50);
                    }

                    Velo velo = new Velo(posX, posY, width, height, minimumBrightness, conversionFactor, brigthtnessPoint,
                            externalVeilCards, externalVeilCommand);
                    velo.establecerParametrosIniciales();
                    String[] singleOids = oids.split(SEPARATOR);
                    
                    if(!externalVeilResponse.isEmpty() && !externalVeilCards.isEmpty()){
                        String firstCharOfCommand = externalVeilResponse.trim().substring(0, 1);
                        String[] singleExternalVeilCard = externalVeilCards.split(SEPARATOR);

                        for (String veilCard : singleExternalVeilCard) {
                            int pos = veilCard.indexOf(Velo.CARD_FACE_SEPARATOR_FOR_VEIL);

                            if (pos != -1) {
                                String faceId = veilCard.substring(0, pos);
                                String cardId = veilCard.substring(pos + 1);
                                QueryIdentifier queryIdentifier = new QueryIdentifier(faceId, cardId, firstCharOfCommand);
                                Configuration.getInstance().addCommandResponseFormat(queryIdentifier, externalVeilResponse);

                            } else {
                                log.warn("Se desecha el external veil card [" + veilCard
                                        + "] porque no tiene la estructura adecuada: faceId-cardId");
                            }
                        }
                    }

                    for (String currentOid : singleOids) {
                        Configuration.getInstance().addVeil(currentOid, velo);
                    }

                } else {
                    log.error("Discarding Unexpected velo. height [" + stringHeight + "] width ["
                            + stringWidth + "] posX [" + stringPosX + "] posY [" + stringPosY + "] minimunBrightness ["
                            + stringMinimumBrightness + "] oids [" + oids + "]");
                }

            } catch (Exception ex) {
                log.error("Discarding Unexpected velo. height [" + stringHeight + "] width ["
                        + stringWidth + "] posX [" + stringPosX + "] posY [" + stringPosY + "] minimunBrightness ["
                        + stringMinimumBrightness + "] oids [" + oids + "]");
            }
        }
    }

    private void readBehaviourOnUsbError(Element doc) {
        String drive;
        int secondsToRestartApp, attemptsToRestartAppBeforeRestartPc, maxAttemptsToRestartPc;

        // Recorremos los atributos del compotamiento ante errores usb
        NodeList usbErrorNode = doc.getElementsByTagName(BEHAVIOUR_ON_USB_ERROR);

        // En principio solo tenemos un elemento
        if (usbErrorNode.getLength() == 1) {
            Element usbError = (Element) usbErrorNode.item(0);

            try {
                drive = usbError.getAttribute(DRIVE).trim();
                secondsToRestartApp = Integer.decode(usbError.getAttribute(SECONDS_TO_RESTART_APP).trim());
                attemptsToRestartAppBeforeRestartPc = Integer.decode(usbError.getAttribute(ATTEMPTS_TO_RESTART_APP_BEFORE_RESTART_PC).trim());
                maxAttemptsToRestartPc = Integer.decode(usbError.getAttribute(MAX_ATTEMPTS_TO_RESTART_PC_BY_CARD).trim());

            } catch (NumberFormatException ex) {
                drive = "D";
                secondsToRestartApp = 10;
                attemptsToRestartAppBeforeRestartPc = 3;
                maxAttemptsToRestartPc = 3;
                log.error("Unexpected usb error description. We assume the following default"
                        + " values: SECONDS TO RESTART APP [10] ATTEMPS TO RESTART APP BEFORE"
                        + " RESTART PC [3] MAX ATTEMPTS TO RESTART PC BY CARD [3] DRIVE [D]");
            }

            Configuration.getInstance().setBehaviourOnUsbError(secondsToRestartApp, attemptsToRestartAppBeforeRestartPc,
                    maxAttemptsToRestartPc, drive);

        } else {
            log.error("Unexpected usb error description.");
        }
    }

    private void readInfoForSwitchingOffDisplay(Element doc) {
        String cardIds, command;

        // Recorremos los atributos del compotamiento ante errores usb
        NodeList switchOffDisplayNode = doc.getElementsByTagName(SWITCH_OFF_DISPLAY);

        // En principio solo tenemos un elemento
        if (switchOffDisplayNode.getLength() == 1) {
            Element switchOffDisplay = (Element) switchOffDisplayNode.item(0);
            cardIds = switchOffDisplay.getAttribute(CARD_IDS).trim();
            command = switchOffDisplay.getAttribute(COMMAND).trim();

            if ((cardIds.length() > 0) && (command.length() > 0)) {
                String[] elements = cardIds.split(SEPARATOR);
                ArrayList<String> cardIdList = new ArrayList<>();

                for (String currentCardId : elements) {
                    cardIdList.add(currentCardId);
                }

                Configuration.getInstance().setInfoForSwitchingOffDisplay(cardIdList, command);

            } else {
                log.error("Discarding unexpected switch off display description.");
            }

        } else {
            log.error("Discarding unexpected switch off display description.");
        }
    }

    private void readInfoSnmpVersionOid(Element doc) {
        NodeList snmpVersionOidNode = doc.getElementsByTagName(SNMP_OID_VERSION);

        // En principio solo tenemos un elemento
        if (snmpVersionOidNode.getLength() == 1) {
            Element snmpOidVersion = (Element) snmpVersionOidNode.item(0);
            String oid = snmpOidVersion.getAttribute(OID).trim();

            if (oid.length() > 0) {
                Configuration.getInstance().addInfoToInfoDisplay(oid, VERSION);

            } else {
                log.error("Discarding unexpected snmp version oid description.");
            }

        } else {
            log.error("Discarding unexpected snmp version oid description.");
        }
    }

    private void readInfoRunAudioTest(Element doc) {
        NodeList commandAudioTestNode = doc.getElementsByTagName(COMMAND_AUDIO_TEST);

        // En principio solo tenemos un elemento
        if (commandAudioTestNode.getLength() == 1) {
            Element snmpOidVersion = (Element) commandAudioTestNode.item(0);
            String command = snmpOidVersion.getAttribute(COMMAND).trim();

            if (command.length() > 0) {
                log.info("Se asociado el comando " + command + " a " + COMMAND_AUDIO_TEST);
                Configuration.getInstance().addInfoToInfoDisplay(COMMAND_AUDIO_TEST, command);

            } else {
                log.error("Discarding unexpected command audio test description.");
            }

        } else {
            log.error("Discarding unexpected command audio test description.");
        }
    }
    
    private void readInfoRunVideoTest(Element doc) {
        NodeList commandVideoTestNode = doc.getElementsByTagName(COMMAND_VIDEO_TEST);

        // En principio solo tenemos un elemento
        if (commandVideoTestNode.getLength() == 1) {
            Element snmpOidVersion = (Element) commandVideoTestNode.item(0);
            String command = snmpOidVersion.getAttribute(COMMAND).trim();

            if (command.length() > 0) {
                log.info("Se asociado el comando " + command + " a " + COMMAND_VIDEO_TEST);
                Configuration.getInstance().addInfoToInfoDisplay(COMMAND_VIDEO_TEST, command);

            } else {
                log.error("Discarding unexpected command video test description.");
            }

        } else {
            log.error("Discarding unexpected command video test description.");
        }
    }

    private void readSnmpNotificationParameters(Element doc) {
        readIdValueAttributes(doc, NOTIFICATIONS, 0, PARAMETER);
    }

    private void readSwCommandsForSnmpNotificationParameters(Element doc) {
        // Buscamos la info de la notificion SNMP
        NodeList notificationsNodeList = doc.getElementsByTagName(NOTIFICATIONS);

        // En principio solo tenemos un elemento
        if (notificationsNodeList.getLength() == 1) {

            NodeList parameterList = ((Element) notificationsNodeList.item(0)).getElementsByTagName(SW_COMMAND_ON_SNMP_SET);

            for (int parameterIndex = 0; parameterIndex < parameterList.getLength(); parameterIndex++) {
                Element currentParameter = (Element) parameterList.item(parameterIndex);
                // .trim() porque me volvió loco cuando puse un espacio delante sin querer en el XML...
                String oid = currentParameter.getAttribute(OID).trim();
                String data = currentParameter.getAttribute(DATA).trim();

                if ((oid.length() > 0) && (data.length() > 0)) {
                    Command currentCommand = new CommandForXML(data, Command.XML);
                    Configuration.getInstance().addCommand(oid, currentCommand);

                } else {
                    log.error("Discarding unexpected Sotfware Command on SNMP SET for SNMP"
                            + " notification parameter with oid [" + oid + "] and data [" + data + "]");
                }
            }

        } else {
            log.error("Discarding unexpected SNMP notification parameters.");
        }
    }

    private void readDirectTextParameters(Element doc) {
        readIdValueAttributes(doc, DIRECT_TEXT, 0, PARAMETER);
    }

    private void readSwCommandsForDirectTextParameters(Element doc) {
        // Buscamos la info de la notificion SNMP
        NodeList notificationsNodeList = doc.getElementsByTagName(DIRECT_TEXT);

        // En principio solo tenemos un elemento
        if (notificationsNodeList.getLength() == 1) {

            NodeList parameterList = ((Element) notificationsNodeList.item(0)).getElementsByTagName(SW_COMMAND_ON_SNMP_SET);

            for (int parameterIndex = 0; parameterIndex < parameterList.getLength(); parameterIndex++) {
                Element currentParameter = (Element) parameterList.item(parameterIndex);
                // .trim() porque me volvió loco cuando puse un espacio delante sin querer en el XML...
                String oid = currentParameter.getAttribute(OID).trim();
                String data = currentParameter.getAttribute(DATA).trim();
                String destination = currentParameter.getAttribute(DESTINATION).trim();

                if (destination.equals(Command.XML)) {
                    if ((oid.length() > 0) && (data.length() > 0)) {
                        Command currentCommand;
                        switch (destination) {
                            case Command.XML:
                                currentCommand = new CommandForXML(data, Command.XML);
                                Configuration.getInstance().addCommand(oid, currentCommand);
                                break;

                            case Command.MEMORY:
                                currentCommand = new CommandForMEMORY(Command.MEMORY);
                                Configuration.getInstance().addCommand(oid, currentCommand);
                                break;

                            default:
                                break;
                        }

                    } else {
                        log.error("Discarding unexpected Sotfware Command on SNMP SET for SNMP"
                                + " notification parameter with oid [" + oid + "] and data [" + data + "]");
                    }

                } else {
                    // no es XML, no hay que hacer nada
                }
            }

        } else {
            log.info("Tag [" + DIRECT_TEXT + "] has not been defined in config file.");
        }
    }

    private void readCommandsForMemoryUpdate(Element doc) {
        // Buscamos la info de la notificion SNMP
        NodeList notificationsNodeList = doc.getElementsByTagName(DIRECT_TEXT);

        // En principio solo tenemos un elemento
        if (notificationsNodeList.getLength() == 1) {

            NodeList parameterList = ((Element) notificationsNodeList.item(0)).getElementsByTagName(SW_COMMAND_ON_SNMP_SET);

            for (int parameterIndex = 0; parameterIndex < parameterList.getLength(); parameterIndex++) {
                Element currentParameter = (Element) parameterList.item(parameterIndex);
                // .trim() porque me volvió loco cuando puse un espacio delante sin querer en el XML...
                String oid = currentParameter.getAttribute(OID).trim();
                String destination = currentParameter.getAttribute(DESTINATION).trim();

                if (destination.equals(Command.MEMORY)) {
                    if (oid.length() > 0) {
                        Command currentCommand = new CommandForMEMORY(Command.MEMORY);
                        Configuration.getInstance().addCommand(oid, currentCommand);

                    } else {
                        log.error("Discarding unexpected Sotfware Command for Memory Update"
                                + " notification parameter with oid [" + oid + "]");
                    }
                } else {
                    // no es MEMORY, no hay que hacer nada
                }
            }

        } else {
            log.info("Tag [" + DIRECT_TEXT + "] has not been defined in config file.");
        }
    }

    public void readIdValueAttributes(Element doc, String childName, int itemNumber, String tagName) {

        // buscamos la info de childName
        NodeList tagNodeList = doc.getElementsByTagName(childName);

        // no podemos pretender leer un item que no existe
        if (itemNumber < tagNodeList.getLength()) {

            NodeList parameterList = ((Element) tagNodeList.item(itemNumber)).getElementsByTagName(tagName);

            for (int parameterIndex = 0; parameterIndex < parameterList.getLength(); parameterIndex++) {
                Element currentParameter = (Element) parameterList.item(parameterIndex);
                String id = currentParameter.getAttribute(ID).trim();
                String value = currentParameter.getAttribute(VALUE).trim();
                String oid = currentParameter.getAttribute(OID).trim();

                if (id.length() > 0) {
                    /* Almacenamos info interna, como la COMMUNITY o ip para notificaciones*/
                    Configuration.getInstance().addInfoToInfoDisplay(id, value);

                    /* Si ademas tenemos oid, es que tenemos que almacenar el valor como
                     informacion por defecto o de inicio para ciertos nodos hoja
                     Ejemplo: la ip y puerto de las notificaciones */
                    if (oid.length() > 0) {
                        Configuration.getInstance().addInfoToInfoDisplay(oid, value);
                    }

                } else {
                    log.error(
                            "Discarding unexpected [" + childName + "." + tagName + "] parameter "
                            + "with id [" + id + "] and value [" + value + "]");
                }
            }

        } else {
            log.info("Tag [" + childName + "] has not been defined in config file.");
        }
    }

    private int computeTimeToExecuteScheduledQuery(String atScheduledTime, String query) {
        /* devuelve un entero con el número de segundos que faltan para ejecutar
         un scheduled query. Si hay error: -1 */

        int timeToGo = -1;

        try {
            /* Obtenemos la hora actual */
            Calendar currentDate = new GregorianCalendar();
            int currentHour = currentDate.get(Calendar.HOUR_OF_DAY);
            int currentMinute = currentDate.get(Calendar.MINUTE);
            int currentSecond = currentDate.get(Calendar.SECOND);

            /* Obtenemos la hora programada */
            String[] elements = atScheduledTime.split(":");
            int scheduledHour = 0, scheduledMinute = 0, scheduledSecond = 0;

            if (elements.length == 3) {
                scheduledHour = Integer.parseInt(elements[0]);
                scheduledMinute = Integer.parseInt(elements[1]);
                scheduledSecond = Integer.parseInt(elements[2]);

            } else if (elements.length == 2) {
                scheduledHour = Integer.parseInt(elements[0]);
                scheduledMinute = Integer.parseInt(elements[1]);

            } else {
                log.error("Error: wrong scheduled time [" + atScheduledTime + "] "
                        + "found in query [" + query + "]");
            }

            int currentTimeInSeconds = ((currentHour * 3600) + (currentMinute * 60) + (currentSecond));
            int scheduledTimeInSeconds = ((scheduledHour * 3600) + (scheduledMinute * 60) + (scheduledSecond));
            scheduledTimeInSeconds = (currentTimeInSeconds > scheduledTimeInSeconds)
                    ? (scheduledTimeInSeconds + ONCE_A_DAY)
                    : scheduledTimeInSeconds;

            timeToGo = currentTimeInSeconds - scheduledTimeInSeconds;
            timeToGo = Math.abs(timeToGo);

        } catch (NumberFormatException ex) {
            log.error("Error: wrong scheduled time [" + atScheduledTime + "] "
                    + "found in query [" + query + "]");
            log.error(ex);
        }

        return timeToGo;
    }

    private void readJFrameCommands(Element doc) {
        NodeList jframeCommandList = doc.getElementsByTagName(COMMAND_BUTTON);

        // Recorremos todos los comandos del jframe
        for (int commandIndex = 0; commandIndex < jframeCommandList.getLength(); commandIndex++) {
            Element currentCommandNode = (Element) jframeCommandList.item(commandIndex);

            /* Leemos todos los atributos del command */
            String id = currentCommandNode.getAttribute(ID).trim();
            String cardId = currentCommandNode.getAttribute(CARD_ID).trim();
            String faceId = currentCommandNode.getAttribute(FACE_ID).trim();
            String command = currentCommandNode.getAttribute(COMMAND).trim();

            /* Analizamos si hay errores en el command */
            if ((id.isEmpty()) || (cardId.isEmpty()) || (faceId.isEmpty()) || (command.isEmpty())) {
                log.error("Error on jframe command with id [" + id + "] card id [" + cardId
                        + "] face id [" + faceId + "] command [" + command + "]");
            } else {
                JFrameCommand jframeCommand = new JFrameCommand(faceId, cardId, command);
                Configuration.getInstance().addJFrameCommand(id, jframeCommand);
            }
        }
    }

    private void readBrightnessProfiles(Element doc) {
        NodeList profileNode = doc.getElementsByTagName(BRIGHTNESS_PROFILES);

        if (profileNode.getLength() == 1) {
            Element currentProfileList = (Element) (profileNode.item(0));
            NodeList profileList = currentProfileList.getElementsByTagName(PROFILE);

            // Recorremos todos los perfiles de brillo
            for (int profileIndex = 0; profileIndex < profileList.getLength(); profileIndex++) {
                int xValue, yValue;
                Element currentProfileNode = (Element) profileList.item(profileIndex);

                /* Leemos todos los atributos del command */
                String id = currentProfileNode.getAttribute(ID).trim();
                String posRefX = currentProfileNode.getAttribute(X_POS_REF).trim();
                String posRefY = currentProfileNode.getAttribute(Y_POS_REF).trim();

                /* Comprobamos que los datos sean numericos */
                try {
                    xValue = Integer.parseInt(posRefX);
                    yValue = Integer.parseInt(posRefY);
                } catch (NumberFormatException ex) {
                    xValue = -1;
                    yValue = -1;
                }

                /* Analizamos si hay errores en el perfile */
                if ((id.isEmpty()) || (posRefX.isEmpty()) || (posRefY.isEmpty()) || (xValue == -1) || (yValue == -1)) {
                    log.error("Discarding on brightness profile with id [" + id + "] pos Ref X [" + posRefX
                            + "] pos Ref Y [" + posRefY + "]");
                } else {
                    Point newPoint = new Point(xValue, yValue);
                    Configuration.getInstance().addBrightnessProfile(id, newPoint);
                }
            }
        } else {
            log.info("Tag [" + BRIGHTNESS_PROFILES + "] has not been defined in config file.");
        }
    }

    private void readSoftwareSnmpSetForCurrentCard(Element currentCard) {
        NodeList swSnmpSetOfCurrentCard = currentCard.getElementsByTagName(SW_SNMP_SET);

        for (int swSnmpSetIndex = 0; swSnmpSetIndex < swSnmpSetOfCurrentCard.getLength(); swSnmpSetIndex++) {
            boolean errorOnParameters = false;
            SoftwareSnmpSet softwareSnmpSet = null;
            Element currentSwSnmpSetNode = (Element) swSnmpSetOfCurrentCard.item(swSnmpSetIndex);

            /* Leemos todos los atributos del xml */
            String oid = currentSwSnmpSetNode.getAttribute(OID).trim();
            String notificationOid = currentSwSnmpSetNode.getAttribute(NOTIFICATION_OID).trim();
            String condition = currentSwSnmpSetNode.getAttribute(CONDITION).trim();
            String bound = currentSwSnmpSetNode.getAttribute(BOUND_VALUE).trim();
            String leafNodeValue = currentSwSnmpSetNode.getAttribute(LEAF_NODE_VALUE).trim();
            int boundIntValue = 0;


            try {
                boundIntValue = Integer.parseInt(bound);

            } catch (NumberFormatException ex) {
                errorOnParameters = true;
                log.error("Error: Software snmp set with OID [" + oid + "] and ABOVE/BELOW condition "
                        + "has undefined bound [" + bound + "] Discarding SW Snmp set.");
            }


            /* Comprobamos que los datos sean correctos */
            if (oid.length() > 0 && notificationOid.length() > 0 && condition.length() > 0 && leafNodeValue.length() > 0 && !errorOnParameters) {

                switch (condition) {
                    case ABOVE:
                        softwareSnmpSet = new SoftwareSnmpSet(notificationOid, leafNodeValue, ConditionChecker.ABOVE, boundIntValue);
                        break;
                        
                    case BELOW:
                        softwareSnmpSet = new SoftwareSnmpSet(notificationOid, leafNodeValue, ConditionChecker.BELOW, boundIntValue);
                        break;

                    default:
                        errorOnParameters = true;
                        log.error("Se descarta el software snmp set porque el tipo"
                                + " de condicion indicado [" + condition + "] es erróneo.");
                }

            } else {
                errorOnParameters = true;
                log.error("Se descarta el software snmp set porque el oid [" + oid 
                        + "], notificationOid [" + notificationOid 
                        + "], condition [" + condition
                        + "], leafNodeValue [" + leafNodeValue
                        + "], o bound ["+ bound + "] no han sido definidos.");
            }

            /* Si no hay errores se procesa */
            if (!errorOnParameters) {
                /* Almacenamos la conficion de notificacion software */
                Configuration.getInstance().addSoftwareSnmpSet(oid, softwareSnmpSet);
            }
        }
    }

    private void readIgnoredSerialPorts(Element doc) {
        NodeList serialPortNode = doc.getElementsByTagName(IGNORE_SERIAL_PORTS);

        for (int index = 0; index < serialPortNode.getLength(); index++) {
            Element serialPort = (Element) serialPortNode.item(index);
            
            try {
                for(String name : serialPort.getAttribute(NAMES).trim().split(",")) {
                    Configuration.getInstance().addIgnoredSerialPortNames(name.trim());
                }
            } catch(NullPointerException ex) {
                log.error("Se descartan los nombre de puertos series a ignorar porque no se ha encontrado la etiqueta ["+ NAMES + "]");
            }
        }
    }

    private void readAdminTextManagers(Element doc) {
        NodeList managers = doc.getElementsByTagName(TEXT_MANAGER);

        /* Leemos todos los checkers */
        for (int managerIndex = 0; managerIndex < managers.getLength(); managerIndex++) {
            Element currentManager = (Element) managers.item(managerIndex);
            String mainOids = currentManager.getAttribute(MAIN_OIDS).trim();
            String textSizeOneLine = currentManager.getAttribute(TEXT_HEIGHT_WHEN_1_LINE).trim();
            String textSizeTwoLines = currentManager.getAttribute(TEXT_HEIGHT_WHEN_2_LINES).trim();
            HashMap<String, String> oidToType = new HashMap<>();
            ArrayList<String> mainOidList = new ArrayList<>();
            ArrayList<String> oidList = new ArrayList<>();
            int integerTextSizeOneLine, integerTextSizeTwoLines;

            /* Ahora leemos todos los oid involucrados en el mensaje  */
            NodeList textParameters = currentManager.getElementsByTagName(TEXT_PARAMETER);
            for (int paramIndex = 0; paramIndex < textParameters.getLength(); paramIndex++) {
                Element currentParam = (Element) textParameters.item(paramIndex);
                String oid = currentParam.getAttribute(OID).trim();
                String type = currentParam.getAttribute(TYPE).trim();
                oidToType.put(oid, type);
                oidList.add(oid);
            }

            /* Creamos un Array List con todos los main oids */
            for (String currentMainOid : mainOids.split(",")) {
                mainOidList.add(currentMainOid.trim());
            }


            /* Comprobamos que los valores de text size son correctos */
            try{
                integerTextSizeOneLine = Integer.parseInt(textSizeOneLine);

                if((integerTextSizeOneLine > 100) || (integerTextSizeOneLine < 1)){
                    log.error("Unexpected integer text size [" + integerTextSizeOneLine + "] when one line on admin text manager.");
                    log.error("Asumming default percentage 60");
                    integerTextSizeOneLine = 60;
                }
                        
            }catch(NumberFormatException ex){ 
                log.error("Unexpected integer text size [" + textSizeOneLine + "] when one line on admin text manager.");
                log.error("Asumming default percentage 60");
                integerTextSizeOneLine = 60;
            }
            
            
            try{
                integerTextSizeTwoLines = Integer.parseInt(textSizeTwoLines);

                if((integerTextSizeTwoLines > 49) || (integerTextSizeTwoLines < 1)){
                    log.error("Unexpected integer text size [" + integerTextSizeTwoLines + "] when two lines on admin text manager.");
                    log.error("Asumming default percentage 40");
                    integerTextSizeTwoLines = 40;
                }
                        
            }catch(NumberFormatException ex){ 
                log.error("Unexpected integer text size [" + textSizeTwoLines + "] when two lines on admin text manager.");
                log.error("Asumming default percentage 40");
                integerTextSizeTwoLines = 40;
            }
            
            
            /* Creamos el manager */
            AdminTextManager manager = new AdminTextManager(mainOidList, oidToType, integerTextSizeOneLine, integerTextSizeTwoLines);

            /* Registramos el manager para cada oid involucrado */
            for (String currentOid : oidList) {
                Configuration.getInstance().addAdminTextManager(currentOid, manager);
            }
        }
    }

    public boolean changeAttributeNameValueTypeInXmlConfiguration(
            /* abrir un archivo 'xml' con la configuración del panel
             y buscar el contenido de uno de sus elementos, del tipo
             hijo-parámetro/atributo/valor, para cambiar el contenido de valor
             ej: config.xml->NOTIFICATIONS->PARAMETER->ID="IP_PORT" VALUE="161" */
            String fileName,
            String element,
            int itemNumber,
            String childName,
            String attributeName,
            String attributeNameContent,
            String attributeValueName,
            String newAttributeValueContent) {

        String nombre;
        String valor;
        boolean attributeHasntBeenFound;

        try {
            String filepath = fileName;
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            Node notifications = doc.getElementsByTagName(element).item(itemNumber);
            NodeList hijos = notifications.getChildNodes();
            for (int i = 0; i < hijos.getLength(); i++) {
                Node hijo = hijos.item(i);
                attributeHasntBeenFound = true;

                if (!"#text".equals(nombre = hijo.getNodeName())) {
                    NamedNodeMap attr = hijo.getAttributes();

                    for (int j = 0; j < attr.getLength(); j++) {
                        Node attrHijo = attr.item(j);

                        ////// tarea: hacerlo con get attribute porque el orden importa
                        if (attributeHasntBeenFound) {
                            if (attributeName.equals(attrHijo.getNodeName())) {
                                if (attributeNameContent.equals(attrHijo.getNodeValue())) {
                                    attributeHasntBeenFound = false;
                                }
                            }

                        } else {
                            if (attributeValueName.equals(attrHijo.getNodeName())) {
                                attrHijo.setTextContent(newAttributeValueContent);

                                // write the content into xml file
                                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                                Transformer transformer = transformerFactory.newTransformer();
                                DOMSource source = new DOMSource(doc);
                                StreamResult result = new StreamResult(new File(filepath));
                                transformer.transform(source, result);

                                log.info("change [" + attributeNameContent + ": " + newAttributeValueContent + "] in XML attribute succeeded.");

                                return true;
                            } else {
                                //log.info("attributeValueName [" + attributeValueName + "] attrHijo.getNodeName() ["
                                //      + attrHijo.getNodeName() + "]");
                            }
                        }
                    }
                }
            }

        } catch (ParserConfigurationException ex) {
            log.error("ParserConfigurationException: " + ex);
        } catch (SAXException ex) {
            log.error("SAXException: " + ex);
        } catch (IOException ex) {
            log.error("IOException: " + ex);
        } catch (TransformerConfigurationException ex) {
            log.error("TransformerConfigurationException: " + ex);
        } catch (TransformerException ex) {
            log.error("TransformerException: " + ex);
        }

        return false;
    }
}