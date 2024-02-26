/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;

import configuration.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;
import socket.Client;

/**
 *
 * @author Tecnovision
 */
public class AdminTextManager {

    private static final Logger log = Logger.getLogger(AdminTextManager.class.getName());

    private final static String FONT_NAME = "FONT_NAME";
    private final static String FONT_STYLE = "FONT_STYLE";
    private final static String FONT_COLOR = "FONT_COLOR";
    private final static String TEXT_HEADER = "TEXT_HEADER";
    private final static String TEXT_MESSAGE = "TEXT_MESSAGE";
    private final static String LIFESPAN = "LIFESPAN";

    private final ArrayList<String> mainOids;
    private final HashMap<String, String> values;
    private final HashMap<String, String> oidToType;
    private final float textSizeOneLine;
    private final float textSizeTwoLines;

    private Timer timer;
    private final Client socket;

    public AdminTextManager(ArrayList<String> mainOids, HashMap<String, String> oidToType, int textSizeOneLine, int textSizeTwoLines) {
        this.mainOids = mainOids;
        this.oidToType = oidToType;
        this.values = new HashMap<>();
        this.socket = new Client();
        this.timer = new Timer(true);
        this.textSizeOneLine = textSizeOneLine/100.0f;
        this.textSizeTwoLines = textSizeTwoLines/100.0f;
    }


    /* Este método nunca va a mandar un mensaje por socket ya que lo único
       que hace es cargar los valores por defecto de los parámetros del xml */
    public void initialUpdate(String oid, String value) {
        String currentType = oidToType.get(oid);

        if (currentType != null) {
            System.out.println("Updating " + currentType + " with " + value);
            values.put(currentType, value);

        } else {
            log.error("Unexpected oid [" + oid + "] to update AdminTextManager");
        }
    }

    
    public void update(String oid, String value, SnmpAgent agent) {
        String currentType = oidToType.get(oid);

        if (currentType != null) {
            System.out.println("Updating " + currentType + " with " + value);
            values.put(currentType, value);

            if (mainOids.contains(oid)) {
                this.generateSocketString(agent);
            }

        } else {
            log.error("Unexpected oid [" + oid + "] to update AdminTextManager");
        }
    }

    private void generateSocketString(SnmpAgent agent) {

        String socketPackage;

        String textMessage = values.get(TEXT_MESSAGE);

        if ((textMessage != null) && (!textMessage.isEmpty())) {
            int lifespan;

            try{
                lifespan = Integer.parseInt(values.get(LIFESPAN));
                
            }catch(Exception e){
                log.error(" getting lifespan: " + e);
                log.error("Asumming lifespan = 1.");
                lifespan = 1;
            }

            timer.cancel();
            timer = new Timer(true);
            timer.schedule(new AdminTextTimer(socket, mainOids, agent), (lifespan * 1000 * 60));

            // Obtenemos todos los valores
            String fontName = this.getFontName();
            String fontStyle = this.getFontStyle();
            String fontColor = this.getFontColor();

            // Lo primero es ver si tenemos header
            String textHeader = values.get(TEXT_HEADER);

            if ((textHeader != null) && (!textHeader.isEmpty())) {
                // hay header, se presentarán dos líneas

                int rows = Configuration.getInstance().getPanelRows();
                //int textSize = (int) (rows * 0.4);   // 40% + 20% + 40%
                int textSize = (int) (rows * textSizeTwoLines);   // 40% + 20% + 40%
                int posY1 = 0;
                //int posY2 = (int) (rows * 0.6);
                int posY2 = (int) (rows * (1 - textSizeTwoLines)) + 1;
                String fontSize = "[SIZE=" + textSize + "]";

                socketPackage = Configuration.STX + "DAT" + (char) 0x19;
                socketPackage += "0";
                socketPackage += "x" + posY1 + (char) 0x19;
                socketPackage += "[CMD=CENTER]" + fontSize + fontName + fontStyle + fontColor + textHeader;
                socketPackage += "$" + (char) 0x03;
                log.info("Mensaje a enviar por socket [" + socketPackage + "]");

                if (socket.sendMessage(socketPackage)) {
                    log.error("showing the admin text....");
                }

                // y ahora la línea de abajo, con el mensaje
                fontSize = "[SIZE=" + textSize + "]";
                socketPackage = Configuration.STX + "DAT" + (char) 0x19;
                socketPackage += Configuration.getInstance().getPanelColumns();
                socketPackage += "x" + posY2 + (char) 0x19;
                socketPackage += "[CMD=SCROLL]" + fontSize + fontName + fontStyle + fontColor + textMessage;
                socketPackage += "$" + (char) 0x03;
                log.info("Mensaje a enviar por socket [" + socketPackage + "]");

                if (socket.sendMessage(socketPackage)) {
                    log.error("showing the admin text....");
                }

            } else {
                // no hay header, se presentará una única línea

                int rows = Configuration.getInstance().getPanelRows();
                //int textSize = (int) (rows * 0.6);   // 20% + 60% + 20%
                int textSize = (int) (rows * textSizeOneLine);   // 20% + 60% + 20%
                int posY = ((rows - textSize) / 2) + 1;
                String fontSize = "[SIZE=" + textSize + "]";
                socketPackage = Configuration.STX + "DAT" + (char) 0x19;
                socketPackage += Configuration.getInstance().getPanelColumns();
                socketPackage += "x" + posY + (char) 0x19;
                socketPackage += "[CMD=SCROLL]" + fontSize + fontName + fontStyle + fontColor + textMessage;
                socketPackage += "$" + (char) 0x03;
                log.info("Mensaje a enviar por socket [" + socketPackage + "]");

                if (socket.sendMessage(socketPackage)) {
                    log.error("showing the admin text....");
                }
            }

        }else if ((textMessage != null) && (textMessage.isEmpty())) {
            timer.cancel();
            socketPackage = Configuration.STX + "CMD" + (char) 0x19 + "CLS$" + Configuration.ETX;
            log.info("Se envia [" + socketPackage + "]");
            if (socket.sendMessage(socketPackage)) {
                log.error("removing the admin text....");
            }
        }
    }

    private String getFontName() {
        String fontName = values.get(FONT_NAME);
        fontName = ((fontName != null) && (!fontName.isEmpty())) ? ("[FONT=" + fontName + "]") : "";
        return fontName;
    }

    private String getFontStyle() {
        String fontStyle = values.get(FONT_STYLE);

        try {
            int fontStyleInt = Integer.parseInt(fontStyle);

            switch (fontStyleInt) {
                case 0:
                    fontStyle = "[NOR]";
                    break;

                case 1:
                    fontStyle = "[NEG]";
                    break;

                case 2:
                    fontStyle = "[CUR]";
                    break;

                case 3:
                    fontStyle = "[NEG][CUR]";
                    break;

                default:
                    fontStyle = "";
                    log.warn("Ignoring unexpected font style value [" + fontStyle + "].");
            }

        } catch (Exception e) {
            fontStyle = "";
            log.warn("Ignoring unexpected font style value [" + fontStyle + "].");
        }

        return fontStyle;
    }

    private String getFontColor() {
        String fontColor = values.get(FONT_COLOR);

        if (fontColor != null) {
            String[] elements = fontColor.split(",");

            if (elements.length == 3) {

                try {
                    int r = Integer.parseInt(elements[0].trim());
                    int g = Integer.parseInt(elements[1].trim());
                    int b = Integer.parseInt(elements[2].trim());

                    if ((r >= 0) && (g >= 0) && (b >= 0)
                            && (r <= 255) && (g <= 255) && (b <= 255)) {

                        fontColor = "[C" + r + "," + g + "," + b + "]";

                    } else {
                        fontColor = "";
                        log.warn("Ignoring unexpected font color value [" + fontColor + "].");
                    }

                } catch (NumberFormatException e) {
                    fontColor = "";
                    log.warn("Ignoring unexpected font color value [" + fontColor + "].");
                }

            } else {
                fontColor = "";
                log.warn("Ignoring unexpected font color value [" + fontColor + "].");
            }

        } else {
            fontColor = "";
            log.warn("Ignoring unexpected font color value [" + fontColor + "].");
        }

        return fontColor;
    }
}
