/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration;


import java.awt.Dimension;
import java.awt.Point;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.swing.JFrame;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.ArrayList;
import launcher.LAUNCHER;





/**
 *
 * @author José
 */
public class Utilities {
    
    private static Utilities instance = null;
    private final Object mutexForFileAccess;
    public static Logger log = Logger.getLogger(Utilities.class.getName());
    
    
    private Utilities(){
        mutexForFileAccess = new Object();
    }
    
    public static Utilities getInstance(){
        if(instance == null){
            instance = new Utilities();
        }

        return instance;
    }

    public void ShutDownPC(String parametros) {
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("shutdown " + parametros);
        } catch (IOException ex) {
            log.error(ex);
        }
    }
    
    public void RestartAPP() {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File currentJar = null;
        try {
            currentJar = new File(LAUNCHER.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            log.error(ex);
        }

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar")) {
            log.error("no es JAR!");
            return;
        }

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        
        
        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException ex) {
            log.error(ex);
        }
        System.exit(0);
    }

    public int getCurrentCounter(String filename, boolean createIfNoExists){
        String line;
        int currentCounterToRestartApp = -1;

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);
            
            while ((line = br.readLine()) != null) {
                log.info("Linea [" + line + "] filename [" + filename + "]");
                line = line.trim();

                if(!line.isEmpty()){
                    try{
                        currentCounterToRestartApp = Integer.parseInt(line);

                    }catch(NumberFormatException ex){
                        log.info("Se supone que es la primera vez que se reinicia la app");
                    }
                }
            }
            
            br.close();

        } catch (FileNotFoundException ex) {
            if(createIfNoExists){
                log.error("Error fichero " + filename + " no encontrado");
            }
        } catch (IOException ex) {
            log.error("Error al leer de fichero " + filename);
        }

        /* fichero corrupto, se reescribe */
        if((currentCounterToRestartApp == -1) && (createIfNoExists)){
            
            synchronized(mutexForFileAccess){
                File file = new File(filename);
                File folder = file.getParentFile();
                if((folder != null) && (!folder.exists())){
                    folder.mkdirs();
                }

                PrintWriter writer = null;

                try {
                    writer = new PrintWriter(filename, "UTF-8");

                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    log.error("ERROR al escribir en fichero " + filename + ": " + ex);
                }

                if (writer != null) {
                    log.info("Se reescribe el fichero [" + filename + "] con un 0 por estar corrupto");
                    writer.println("0");
                    writer.close();
                }

                currentCounterToRestartApp = 0;
                mutexForFileAccess.notifyAll();
            }
        }

        return currentCounterToRestartApp;
    }

    public void increaseCounterFile(String filename, int currentCounter){
        PrintWriter writer = null;
        currentCounter += 1;

        try {
            writer = new PrintWriter(filename, "UTF-8");

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            log.error("ERROR al escribir en fichero " + filename + ": " + ex);
        }

        if(writer != null){
            log.info("Se escribe el valor [" + currentCounter + "] en [" + filename + "]");
            writer.println(String.valueOf(currentCounter));
            writer.close();
        }
    }

    public static HashMap readIni(String nombreFichero,
            ArrayList<String> defaultConfiguration,
            String separador,
            String comentario) {

        // lee nombreFicheroIni y lo devuelve como HashMap
        // si no existe lo rellena con las lineas de la Lista pasada

        File fichero = new File(nombreFichero);
        
        // si no existe fichero de configuración...
        // lo creamos
        if (!fichero.exists()) {
            try {
                Files.write(fichero.toPath(),
                        defaultConfiguration,
                        Charset.defaultCharset(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException ex) {
                System.out.println("ERROR al escribir en fichero " + nombreFichero);
            }
        }
         
        // y después, tanto si existía como si no...
        // lo leemos
        
            // antes...
            // vamos a imaginar que existía, en principio no vamos a utilizar...
            // la defaultConfiguration, pero alguna variable que se requiere es nueva...
            // o alguien ha borrado la linea del archivo de configuración...
            // en ese caso habría que dar de alta el nuevo valor por defecto...
            // entra las líneas del config.txt
            // para ello vamos a ir generando un ArrayList por si acaso
            // que después ha habido error..., pues se genera el archivo nuevo...
            // con el contenido antiguo más el default individual
            //ArrayList<String> porSiAcaso = new ArrayList<>();
        
        // configuratiom es el HashMap que vamos a devolver tras la lectura
        HashMap<String, String> configuration = new HashMap<>();
        String cadena;

        try (FileReader fr = new FileReader(fichero)) {
            BufferedReader br = new BufferedReader(fr);
            while ((cadena = br.readLine()) != null) {
                cadena = cadena.trim();
                //porSiAcaso.add(cadena);

                if((cadena.length() > 0) && (!cadena.substring(0, comentario.length()).equals(comentario))){

                    // si se salvó el campo vacío, el '=' al final genera solamente un elements[0]
                    if(cadena.endsWith("=")){
                        // añadiendo un espacio ya genera dos (luego, con trim, se quedará otra vez vacío)
                        cadena+=" ";
                    }
                    String[] elements = cadena.split(separador);

                    if(elements.length != 2){
                        System.out.println("ERROR numero de elemento erroneo: " + cadena);

                    }else{
                        elements[0] = elements[0].trim();
                        elements[1] = elements[1].trim();
                        
                        if(elements[1].contains(comentario)){
                            elements[1] = elements[1].substring(0, elements[1].indexOf(comentario));
                            elements[1] = elements[1].trim();
                        }
                        
                        configuration.put(elements[0], elements[1]);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error fichero " + nombreFichero + " no encontrado");
        } catch (IOException ex) {
            System.out.println("Error al leer de fichero " + nombreFichero);
        }

        return configuration;
    }
    

    public void deleteFile(String path){
        synchronized(mutexForFileAccess){
            try{
            
                File file = new File(path);
                if(file.delete()){
                    log.info("Se ha borrado el fichero " + path);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            
            mutexForFileAccess.notifyAll();
        }
    }

    public void deleteFilesOnCurrentFolder(String prefixFilename, String path){
        synchronized(mutexForFileAccess){
            File dir = new File(path);

            if(dir.exists()){
                for(File file: dir.listFiles()){
                    if(file.getName().startsWith(prefixFilename)){
                        file.delete();
                        log.info("Se borra el fichero [" + file.getName() + "]");
                    }
                }
            }

            mutexForFileAccess.notifyAll();
        }
    }
    
    public void deleteFolder(String path){
        synchronized(mutexForFileAccess){
            File folder = new File(path);
            if ((folder.listFiles() != null) && (folder.listFiles().length == 0)) {
                folder.delete();
                log.info("Se borra el directorio " + path);
            }
            
            mutexForFileAccess.notifyAll();
        }
    }

    public static void copyFile(String source, String target){
        try{
            Path sourcePath = Paths.get(source);
            Path targetPath = Paths.get(target);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

    	}catch(Exception e){
            e.printStackTrace();
    	}
    }
    
    
    public Point getPosition(String filename){
        Point point = new Point(-1, -1);
        String line;

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if ((!line.isEmpty()) && (!line.startsWith("#"))) {

                    try {
                        String[] elements = line.split("=");
                        
                        if((elements != null) && (elements.length == 2) && (elements[0].equals("x"))){
                            point.x = Integer.parseInt(elements[1]);
                            
                        }else if((elements != null) && (elements.length == 2) && (elements[0].equals("y"))){
                            point.y = Integer.parseInt(elements[1]);
                        }

                    } catch (NumberFormatException ex) {
                        point = new Point(-1, -1);
                    }
                }
            }

            br.close();

        } catch (FileNotFoundException ex) {
            //log.error("Error fichero " + filename + " no encontrado");
           
        } catch (IOException ex) {
            //log.error("Error al leer de fichero " + filename);
        }
        
        return point;
    }
    
    
    public void writePosition(int x, int y, String filename){
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(filename, "UTF-8");

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            log.error("ERROR al escribir en fichero " + filename + ": " + ex);
        }

        if(writer != null){
            writer.println("# Coordenadas de la posicion del Manager");
            String lineX = "x=" + x;
            String lineY = "y=" + y;
            writer.println(lineX);
            writer.println(lineY);
            writer.close();
        }
    }

    //taskkill /im myprocess.exe /f
   //taskkill /pid 1234 /f
    public static void killProcessByName(String name){ 

         try{ 
            String cmd = null;
            
            Runtime rt = Runtime.getRuntime();
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1){
                cmd = "cmd /c taskkill /im " + name + " /f";
            }
            //else   // para Linux
                //rt.exec("kill -9 " +....);*/
            
            // Se lanza el ejecutable. 
            Process process=Runtime.getRuntime().exec (cmd); 
                    
             // Se obtiene el stream de salida del programa 
             InputStream is = process.getInputStream(); 
              
             // Se prepara un bufferedReader para poder leer la salida más comodamente.
             //BufferedReader br = new BufferedReader (new InputStreamReader(is),"UTF-8"); 
             BufferedReader br = new BufferedReader (new InputStreamReader(is)); 
             // Se lee la primera linea 
             String aux = br.readLine(); 
              
             // Mientras se haya leido alguna linea 
             while (aux!=null) 
             { 
                 // Se escribe la linea en pantalla 
                 log.info(aux); 
                  
                 // y se lee la siguiente. 
                 aux = br.readLine(); 
             }
             
             
         }  
         catch (Exception e) 
         { 
             // Excepciones si hay algún problema al arrancar el ejecutable o al leer su salida.
             /////
             e.printStackTrace(); 
         }
     }
     
    
    // Guarda (Me)   ...escritura
    public static void setLocation(JFrame formulario) {

        File fichero = new File(formulario.getName() + ".ini");

        ArrayList<String> currentLocation = new ArrayList<>();
        currentLocation.add("posX = " + ((int) formulario.getLocationOnScreen().getX()));
        currentLocation.add("posY = " + ((int) formulario.getLocationOnScreen().getY()));
        currentLocation.add("width = " + ((int) formulario.getSize().width));
        currentLocation.add("height = " + ((int) formulario.getSize().height));

        try {
            Files.write(fichero.toPath(),
                    currentLocation,
                    Charset.defaultCharset(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            System.out.println("ERROR al escribir en fichero " + formulario.toString());
        }
    }
   
    // Posiciona (Me)   ...lectura
    public static void getLocation(JFrame formulario){
        File fichero = new File(formulario.getName() + ".ini");
        Point currentLocation = null;
        String cadena;
        HashMap<String, String> params = new HashMap<>();

        if (fichero.exists()) {
            try (FileReader fr = new FileReader(fichero)) {
                BufferedReader br = new BufferedReader(fr);
                while ((cadena = br.readLine()) != null) {
                    cadena = cadena.trim();

                    String[] elements = cadena.split("=");

                    if(elements.length != 2){
                        System.out.println("ERROR numero de elemento erroneo: " + cadena);

                    }else{
                        elements[0] = elements[0].trim();
                        elements[1] = elements[1].trim();

                        params.put(elements[0], elements[1]);
                    }
                    
                }
            } catch (FileNotFoundException ex) {
                System.out.println("Error fichero " + formulario.toString() + " no encontrado");
            } catch (IOException ex) {
                System.out.println("Error al leer de fichero " + formulario.toString());
            }
        }
        
        String x = params.get("posX");
        String y = params.get("posY");
        
        if((x != null) && (y != null)){
            formulario.setLocation(new Point(Integer.parseInt(x), Integer.parseInt(y)));
        } else {
            formulario.setLocationRelativeTo(null);
        }
    }

    // Dimensiona (Me)   ...lectura
    public static void getDimension(JFrame formulario){
        File fichero = new File(formulario.getName() + ".ini");
        Dimension currentDimension = null;
        String cadena;
        HashMap<String, String> params = new HashMap<>();

        if (fichero.exists()) {
            try (FileReader fr = new FileReader(fichero)) {
                BufferedReader br = new BufferedReader(fr);
                while ((cadena = br.readLine()) != null) {
                    cadena = cadena.trim();

                    String[] elements = cadena.split("=");

                    if(elements.length != 2){
                        System.out.println("ERROR numero de elemento erroneo: " + cadena);

                    }else{
                        elements[0] = elements[0].trim();
                        elements[1] = elements[1].trim();

                        params.put(elements[0], elements[1]);
                    }
                    
                }
            } catch (FileNotFoundException ex) {
                System.out.println("Error fichero " + formulario.toString() + " no encontrado");
            } catch (IOException ex) {
                System.out.println("Error al leer de fichero " + formulario.toString());
            }
        }
        
        String width = params.get("width");
        String height = params.get("height");
        
        if((width != null) && (height != null)){
            formulario.setSize(new Dimension(Integer.parseInt(width), Integer.parseInt(height)));
        }
    }
}