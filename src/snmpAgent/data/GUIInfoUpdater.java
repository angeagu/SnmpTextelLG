/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;

import configuration.Configuration;
import javax.swing.JLabel;
import static launcher.LAUNCHER.SHOW_GUI_MANAGER;
import org.apache.log4j.Logger;
import snmpAgent.Velo;
import userLayer.ButtonPair;
import userLayer.ManagerForLG;

/**
 *
 * @author Tecnovision
 */
public class GUIInfoUpdater {

    private final static String JLABEL = "JLabel";
    private final static String BUTTON_PAIR = "ButtonPair";
    private final static String INTEGER = "Integer";
    private final static String STRING = "String";
    private final static String VEIL = "Veil";
    private final static String SEPARATOR = "=";
    
    private final String sufix;
    private final double factor;
    private final String componentName;
    private final String dataType;
    private final String componentType;
    private final String option1;
    private final String option2;
    private final String oid;

    private static final Logger log = Logger.getLogger(GUIInfoUpdater.class.getName());

    public GUIInfoUpdater(String sufix, double factor, String componentNane,
            String dataType, String componentType, String option1,
            String option2, String oid) {
        this.sufix = sufix;
        this.factor = factor;
        this.componentName = componentNane;
        this.dataType = dataType;
        this.componentType = componentType;
        this.option1 = option1;
        this.option2 = option2;
        this.oid = oid;
    }


    public void update(String value) {
        if (componentType.equalsIgnoreCase(JLABEL)) {
            JLabel label = SHOW_GUI_MANAGER ? ManagerForLG.getInstance().getJLabelFromComponentName(componentName) : null;
            if (label != null) {

                if (dataType.equalsIgnoreCase(INTEGER)) {
                    String text;

                    try {
                        int currentValue = Integer.parseInt(value);
                        currentValue = (int) (currentValue * factor);
                        text = currentValue + sufix;

                    } catch (NumberFormatException ex) {
                        text = "?";
                    }
                        
                    label.setText(text);
                } else if (dataType.equalsIgnoreCase(STRING)) {
                    String text = value;

                    int posicionDelIgual1 = option1.indexOf(SEPARATOR);
                    int posicionDelIgual2 = option2.indexOf(SEPARATOR);
                    if ((posicionDelIgual1 != -1) && (posicionDelIgual2 != -1)) {
                        if (value.equalsIgnoreCase(option1.substring(0, posicionDelIgual1))) {
                            text = option1.substring(posicionDelIgual1 + 1);
                        } else if (value.equalsIgnoreCase(option2.substring(0, posicionDelIgual2))) {
                            text = option2.substring(posicionDelIgual2 + 1);
                        }
                    }

                    if(text.isEmpty()){
                        text = "?";
                    }

                    label.setText(text);
                                       
                } else if (dataType.equalsIgnoreCase(VEIL)){
                    String text;

                    try {
                        /* Info de la lectura del sensor normalizado */
                        int currentValue = Integer.parseInt(value);
                        currentValue = (int) (currentValue * factor);
                        text = currentValue + sufix;
                        
                        /* Info del velo */
                        Velo velo = Configuration.getInstance().getVeil(oid);

                        if(velo != null){
                            text += " (" + String.valueOf(velo.getCurrentBrightness()) + "%)";
                        }

                    } catch (NumberFormatException ex) {
                        text = "?";
                    }

                    label.setText(text);
                    
                } else {
                    log.error("Unexpected data type en el updater: " + dataType);
                }
            } else if (SHOW_GUI_MANAGER) {
                log.error("No se ha encontrado el componentName [" + componentName + "] en el manager.");
            }

        } else if (componentType.equalsIgnoreCase(BUTTON_PAIR)) {
            ButtonPair button = ManagerForLG.getInstance().getJButtonFromComponentName(componentName);

            if (button != null) {
                
                if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("01")) {
                    
                    button.turnOn();

                } else if (value.equalsIgnoreCase("0") || value.equalsIgnoreCase("00")) {
                    
                    button.turnOff();

                } else {
                    
                    button.unknownState();
                }

            } else {
                log.error("No se ha encontrado el componentName [" + componentName + "] en el manager.");
            }

        } else {
            log.error("Unexpected component type [" + componentType + "] for [" + componentName + "]");
        }
    }
}
