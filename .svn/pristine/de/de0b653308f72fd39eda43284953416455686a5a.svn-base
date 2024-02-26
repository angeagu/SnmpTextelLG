package snmpAgent;

// v1.0   junio2015
// Clase para poder controlar el brillo de cualquier parte del monitor
// inicialmente para el proyecto de Renfe (pantallas en estaciones de tren)
// ya que todavía no tenemos información del protocolo
// para alterar el brillo en la transmitter box

// esta clase pinta un recuadro relleno de color negro
// recibe como parámetros las coordenadas de la diagonal que define su tamaño
// posX y posY son las coordenadas del punto superior izquierda
// posXX y posYY son las coordenadas del punto inferior derecha
// por defecto el recuadro tendrá opacidad 0, será totalmente transparente
// hay un método 'setOpacidad' que permite establecer el índice de opacidad deseada
// ese índice es cualquier float entre los límites 0 y 1 (0 = transparente, 1 = opaco)

// desde otro prisma más adecuado al concepto BRILLO que manejamos en el mundo del LED
// hay un segundo constructor que recibe también el índice de brillo deseado
// siendo 'brillo' un parámetro entre 0 y 100
// y un algoritmo se encargará de conseguir el resultado deseado manejando la opacidad

// en cualquier caso, hay un segundo método público setBrillo




///// pendiente que pueda ser llamado stand alone (jar)
   // y que arranque con los parámetros del archivo velo.ini
   // y que lo cree si no existe

import configuration.Configuration;
import static configuration.Loader.SEPARATOR;
import configuration.data.CardIdentifier;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JDialog;
import org.apache.log4j.Logger;
import socket.Client;
// JDialog no deja rastro en la barra de tareas !!!
// y por lo tanto no se puede minimizar, ni siquiera con el botón Show Desktop
// ideal para esta aplicación !!!


public class Velo extends JDialog {

    public final static String CARD_FACE_SEPARATOR_FOR_VEIL = "-";
    public final static boolean VELO_AWT = false;
    public final static boolean VELO_FX = false;
    public final static boolean VELO_LG = true;
    
    private final static int X1 = 0;
    private final static int Y1 = 0;
    private final static int X3 = 100;
    private final static int Y3 = 100;
    
    private Client socket;
    private int currentBrightness;
    private final Point brigthtnessPoint;
    private final HashMap<String, Integer> lastReading; // key = OID
    private final int posX, posY, width, height;
    private final int minimumBrightness;
    private final double conversionFactor;
    private final static float OPACIDAD_INICIAL = 0f;
    private final HashMap<Integer, Integer> valuesInBrightnessCurve;
    private final ArrayList<CardIdentifier> externalVeilCards;
    private final String externalVeilCommand;

    static Logger LOG = Logger.getLogger(Velo.class.getName());
    
    // constructor
    public Velo(int posX, int posY, int width, int height, int minimumBrightness,
            double conversionFactor, Point brigthtnessPoint, String externalVeilCards,
            String externalVeilCommand) {
        this.posX=posX;
        this.posY=posY;
        this.width=width;
        this.height=height;
        this.currentBrightness = 0;
        this.lastReading = new HashMap<>();
        this.conversionFactor = conversionFactor;
        this.minimumBrightness= minimumBrightness;
        this.brigthtnessPoint = brigthtnessPoint;
        this.valuesInBrightnessCurve = new HashMap<>();
        this.externalVeilCards = new ArrayList<>();
        this.externalVeilCommand = externalVeilCommand;
        this.socket = null;
        
        /* Calculamos la curva del brillo */
        double x2 = brigthtnessPoint.getX();
        double y2 = brigthtnessPoint.getY();
        double denom = (X1 - x2) * (X1 - X3) * (x2 - X3);
        double A = (X3 * (y2 - Y1) + x2 * (Y1 - Y3) + X1 * (Y3 - y2)) / denom;
        double B = (Math.pow(X3,2) * (Y1 - y2) + Math.pow(x2,2) * (Y3 - Y1) + Math.pow(X1,2) * (y2 - Y3)) / denom;
        double C = (x2 * X3 * (x2 - X3) * Y1 + X3 * X1 * (X3 - X1) * y2 + X1 * x2 * (X1 - x2) * Y3) / denom;
        
        for(int x = 0; x <= 100; x++){
            int y = (int)(Math.rint((A * Math.pow(x,2)) + (B * x) + C));
            valuesInBrightnessCurve.put(x, y);
        }
        
        String[] singleExternalVeilCard = externalVeilCards.split(SEPARATOR);

        for (String veilCard : singleExternalVeilCard) {
            int pos = veilCard.indexOf(CARD_FACE_SEPARATOR_FOR_VEIL);
            
            if(pos != -1){
                String faceId = veilCard.substring(0, pos);
                String cardId = veilCard.substring(pos + 1);
                this.externalVeilCards.add(new CardIdentifier(cardId, faceId));

            }else{
                LOG.warn("Se desecha el external veil card [" + veilCard + 
                        "] porque no tiene la estructura adecuada: faceId-cardId");
            }
        }
    }

    public void establecerParametrosIniciales() {
        if(VELO_AWT){
            getContentPane().setBackground(Color.BLACK);
            setUndecorated(true);
            setLocation(posX, posY);
            setSize(width, height);
            setOpacity(OPACIDAD_INICIAL);
            setAlwaysOnTop(true);
            setVisible(true);
            MyStaticImage staticImage = new MyStaticImage(posX, posY, width, height);

        }else if(VELO_FX){
            socket = new Client();
        }
    }

    public void setBrillo(String oid, int brillo, SnmpAgent agent) {
        //setAlwaysOnTop(true);
        boolean isFirstReading = (lastReading.get(oid) == null);
        int normalizedBrightness = (int)(brillo * conversionFactor);
        lastReading.put(oid, normalizedBrightness);
        int worseNormalizedBrightness = 1;
        int newBrightnessValue;

        /* Obtenemos el brillo mas elevado de las ultimas lecturas */
        for(Iterator<Integer> it = lastReading.values().iterator(); it.hasNext(); ){
            int currentValue = it.next();
            
            if(currentValue > worseNormalizedBrightness){
                worseNormalizedBrightness = currentValue;
            }
        }

        /* Aplicamos la ecuacion de la curva para ver a que valor de brillo se corresponde la lectura */
        int computedValueForVeil = valuesInBrightnessCurve.get(worseNormalizedBrightness);

        /* Calculamos el nuevo valor de brillo */
        if(isFirstReading){
            newBrightnessValue = Math.min(computedValueForVeil, 100);

        }else{
            if(computedValueForVeil > currentBrightness){
                newBrightnessValue = currentBrightness+1;

            }else if(computedValueForVeil < currentBrightness){
                newBrightnessValue = currentBrightness-1;
        
            }else{
                newBrightnessValue = currentBrightness;
            }
        }

       
        /* Cambiamos el brillo en caso de ser necesario */
        if((isFirstReading) || ((newBrightnessValue != currentBrightness) && (newBrightnessValue > minimumBrightness))){
            if (newBrightnessValue >= 0 && newBrightnessValue <= 100) {   // integer entre 0 y 100
                if (newBrightnessValue != 0 && newBrightnessValue < minimumBrightness) {
                    newBrightnessValue = minimumBrightness;
                }

                currentBrightness = newBrightnessValue;
                this.setOpacidad(convertirBrilloEnOpacidad(newBrightnessValue), agent);
            }
        }
    }

    public void setOpacidad(float opacity, SnmpAgent agent) {
        if (opacity>=0f && opacity<=1f) {   // float entre 0 y 1
            
            if(VELO_AWT){
                setOpacity(opacity);
                
            }else if(VELO_LG){
                
                if (!externalVeilCommand.isEmpty()) {
                    String hexValue = Integer.toHexString(Math.round((1 - opacity) * 100));
                    for (CardIdentifier card : externalVeilCards) {
                        String command = externalVeilCommand.replace("ID", card.getCardId());///// esto deberia ser constante
                        command = command.replace("VALUE", hexValue);///// esto deberia ser constante
                        agent.sendCommand(card.getCardId(), command, card.getFaceId());
                        LOG.info("New lg value: " + (int) Long.parseLong(hexValue, 16) + " para la opacidad: " + opacity);
                    }
                } else {
                    LOG.error("External veil command has not been declared.");
                }

            }else if(VELO_FX){
                String brightness = (currentBrightness >= 100) ? "99" : String.valueOf(currentBrightness);
                String socketPackage = Configuration.STX + "CMD" + (char) 0x19 + "BRG" + brightness + "$" + Configuration.ETX;
                LOG.info("Se envia [" + socketPackage + "]");
                if (socket.sendMessage(socketPackage)) {
                    LOG.error("sending brightness cmd....");
                }
            }
        }
    }

    private float convertirBrilloEnOpacidad(int brillo) {
        float brilloToOpacity;
        brilloToOpacity = (1f - ((float)brillo / 100));   // algoritmo
        return brilloToOpacity;
    }
        
    public int getCurrentBrightness(){
        return currentBrightness;
    }
    
    public void exit(){
        if(VELO_AWT){
            this.dispose();
        }
    }
}