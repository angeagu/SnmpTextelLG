/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launcher;

//import userLayer.Manager;
import configuration.Configuration;
import configuration.Loader;
import hardwareLayer.HardwareDataProcessor;
import configuration.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;
import snmpAgent.Velo;
import userLayer.ManagerForLG;

/**
 *
 * @author Tecnovision
 */
public class LAUNCHER {

    public final static String VERSION = "2.4";

    /* Esta constante indica si se desea mostrar el manager (gui, interfaz gráfica de usuario) 
       del snmp que muestra información del estado de la pantalla, creado originalmente para renfe. 
       Ahora está desactivado porque no tiene sentido tenerlo activado en el entorno de las raspberry pi */
    public final static boolean SHOW_GUI_MANAGER = true;

    /* Esta constante habilita o desabilita el sistema de reinicio de la aplicación y del ordenador llegado 
       al caso si no se consigue comunicar mediante USB con las tarjetas. Este procedimiento se implementó
       para Renfe, por lo que por ahora está desactivado, ya que no es necesario por ahora. */
    public final static boolean REBOOTING_BEHAVIOUR_ENABLED = false;

    private final String TITULO = "Version " + VERSION;
    // 1.2.1 se cambia en las notificaciones software la etiqueta BOUND por BOUNDS (4-02-2016)
    // 1.2.2 se añade 'LOG=ss' en las query, para que dejen rastro en el fichero LOG (1-03-2016) cada 'ss' segundos
    //       se añade la posibilidad de aceptar trapIP o trapPort mediante setSnmp y que se modifique en el XML
    //       se añade mostrar directText como mensaje de alarma, configurable desde setSnmp y que se modifique en el XML
    // 1.3.1 se añade la opción de que el display sea un monitor LG

    /* declaramos un objeto Logger
     la librería log4j permite dejar rastro en un fichero(s)
     en la forma declarada en log4j.properties */
    private static final Logger LOG = Logger.getLogger(LAUNCHER.class.getName());

    // todas las constantes declaradas al principio, para luego no tener que recorrer
    // todo el código para cambiar todas las referencias
    public final static String NOMBRE_FICHERO_INICIALIZACION = "snmpLauncher.ini";

    /* claves para buscar en los archivos de configuración */
    public static String CLAVE_PARA_FICHERO_IDENTIFICACION_PANEL = "xmlIdentification";
    public static String CLAVE_PARA_FICHERO_CONFIGURACION_PANEL = "xmlConfiguration";
    public static String CLAVE_PARA_FICHERO_MIB = "mibFile";

    /* por defecto, por si no existe el fichero de inicialización */
    public static String NOMBRE_DEFAULT_FICHERO_IDENTIFICACION_PANEL = "address.xml";
    public static String NOMBRE_DEFAULT_FICHERO_CONFIGURACION_PANEL = "config.xml";
    public static String NOMBRE_DEFAULT_FICHERO_MIB = "TEXTEL-TLI3.mib";

    String address_filename;
    String config_filename;
    String mibFile_filename;

    public static void main(String[] args) {
        LAUNCHER launcher = new LAUNCHER();
        launcher.readInitialization(NOMBRE_FICHERO_INICIALIZACION);
        launcher.init();
    }

    public void readInitialization(String nombreFichero) {

        LOG.info("\n\n Inicializando aplicación...");

        /* leemos config.txt en un HashMap
         pero si no existe el txt, readini lo generará con el siguiente ArrayList */
        ArrayList<String> contenidoDefault = new ArrayList<>();

        /* es importante comenzar con un comentario (#) porque,
         si luego se edita desde el Explorador, a veces se inserta
         automáticamente un caracter no imprimible
         que altera la lectura de la primera línea */
        contenidoDefault.add("# INITIALIZATION");

        contenidoDefault.add(CLAVE_PARA_FICHERO_IDENTIFICACION_PANEL + " = " + NOMBRE_DEFAULT_FICHERO_IDENTIFICACION_PANEL);
        contenidoDefault.add("# e.g. 'C:\\Textel\\SnmpRenfe\\" + NOMBRE_DEFAULT_FICHERO_IDENTIFICACION_PANEL + "'");
        contenidoDefault.add(CLAVE_PARA_FICHERO_CONFIGURACION_PANEL + " = " + NOMBRE_DEFAULT_FICHERO_CONFIGURACION_PANEL);
        contenidoDefault.add("# e.g. 'C:\\Textel\\SnmpRenfe\\" + NOMBRE_DEFAULT_FICHERO_CONFIGURACION_PANEL + "'");
        contenidoDefault.add(CLAVE_PARA_FICHERO_MIB + " = " + NOMBRE_DEFAULT_FICHERO_MIB);
        contenidoDefault.add("# e.g. 'C:\\Textel\\SnmpRenfe\\" + NOMBRE_DEFAULT_FICHERO_MIB + "'");

        /* declaramos el HashMap parametros y lo rellenamos mediante el método 'readIni' */
        HashMap<String, String> parametros;
        parametros = Utilities.readIni(NOMBRE_FICHERO_INICIALIZACION, contenidoDefault, "=", "#");

        address_filename = parametros.get(CLAVE_PARA_FICHERO_IDENTIFICACION_PANEL);
        config_filename = parametros.get(CLAVE_PARA_FICHERO_CONFIGURACION_PANEL);
        mibFile_filename = parametros.get(CLAVE_PARA_FICHERO_MIB);

        if ((address_filename == null) || (address_filename.length() == 0)) {
            LOG.error("Missing address file [" + address_filename + "]. The APP is going to close");
            System.exit(-1);
        }

        if ((config_filename == null) || (config_filename.length() == 0)) {
            LOG.error("Missing config file [" + config_filename + "]. Closing APP...");
            System.exit(-1);
        }

        if ((mibFile_filename == null) || (mibFile_filename.length() == 0)) {
            LOG.error("Missing mib file [" + mibFile_filename + "]. Closing APP...");
            System.exit(-1);
        }
    }

    public void init() {
        /* a partir de ahora, todas las referencias a la Clase Configuration
         se harán con el método getInstance, ya que esta Clase sigue el
         patrón Singleton (una única instancia), el que se utiliza cuando una serie de datos
         han de estar disponibles para todos los demás objetos de la aplicación,
         o cuando una Clase controla el acceso a un recurso físico único,
         como un ratón, un puerto serie, un fichero abierto en modo exclusivo, etc */

        /* Leemos los ficheros XML de identificación y configuración */
        Configuration.getInstance().readAddress(address_filename);
        Loader.getInstance().readXmlConfiguration(config_filename);

        /* Creamos a nuestro agente, tiene que ser obligatoriamente posterior a la lectura del xml */
        SnmpAgent agent = new SnmpAgent(mibFile_filename);
        //agent.printAgent();

        /* Instanciamos un Manager y le damos la referencia del agente
         para que pueda avisarle de alguna orden clickada
         y desde el constructor de Manager le daremos su propia referencia al agente,
         que es el que sabe qué pasa, para que mantenga el manager actualizado
         */
        if (SHOW_GUI_MANAGER) {
            ManagerForLG.getInstance().setSnmpAgent(agent);
        }

        /* Creamos tantos procesadores como caras tengamos */
        HashMap<String, TreeSet<String>> cardIdsByFaceId = Configuration.getInstance().getCardIdsByFaceId();
        ArrayList<HardwareDataProcessor> processorFaces = new ArrayList<>();
        
        for (Iterator<String> it = cardIdsByFaceId.keySet().iterator(); it.hasNext();) {
            String currentFaceId = it.next();
            TreeSet<String> cards = cardIdsByFaceId.get(currentFaceId);
            HardwareDataProcessor currentHardwareDataProcessor = new HardwareDataProcessor(currentFaceId, cards, agent);
            processorFaces.add(currentHardwareDataProcessor);
            agent.addHardwareDataProcessor(currentHardwareDataProcessor, cards, currentFaceId);
        }
        
        // Comprobamos si podemos mandar notificaciones
        agent.checkCandSendSnmpNotification();

        // Lanzamos todos los procesadores
        for (HardwareDataProcessor currentDataProcessor : processorFaces) {
            currentDataProcessor.processorIsInitialized();
            currentDataProcessor.start();
        }

        // Mostramos el manager
        if (SHOW_GUI_MANAGER) {
            ManagerForLG.getInstance().setVisible(true);
            ManagerForLG.getInstance().setTitle(TITULO);
        }

        // Ahora mostramos el velo
        HashMap<String, Velo> veils = Configuration.getInstance().getVeils();

        for (String currentOid : veils.keySet()) {
            agent.setVelo(currentOid, veils.get(currentOid));
        }

        ///// PRUEBA
        /*
         // vamos a cambiar la IP del administrador (trap) en el config.xml
         System.out.println("prueba: new trapIp succeeded [" + Loader.getInstance().changeAttributeNameValueTypeInXmlConfiguration(
         config_filename,
         "NOTIFICATIONS",
         0,
         "PARAMETER",
         "ID",
         "ADMINISTRATOR_IP_ADDRESS",
         "VALUE",
         "192.168.1.120") + "]");
         */
        // Esperamos a que termine la ejecucion de todos los procesadores
        for (HardwareDataProcessor currentProcessor : processorFaces) {
            if (currentProcessor.isAlive()) {
                try {
                    currentProcessor.join();
                } catch (InterruptedException ex) {
                    LOG.error(ex);
                }
            }
        }

        // Cerramos el cliente snmp
        agent.closeSnmp();

        // Cerramos el manager
        if (SHOW_GUI_MANAGER) {
            ManagerForLG.getInstance().exit();
        }

        // Cerramos los velos
        veils = Configuration.getInstance().getVeils();

        for (Velo currentVeil : veils.values()) {
            currentVeil.exit();
        }
    }
}