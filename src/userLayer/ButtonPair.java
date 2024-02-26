/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLayer;

import javax.swing.JButton;
import java.awt.Color;

/**
 *
 * @author Tecnovision
 */
public class ButtonPair {
    
    private final JButton on;
    private final JButton off;
    private final Color colorLetraOn;
    private final Color colorLetraOff;

    public ButtonPair(JButton on, JButton off, Color colorLetraOn, Color colorLetraOff) {
        this.on = on;
        this.off = off;
        this.colorLetraOn = colorLetraOn;
        this.colorLetraOff = colorLetraOff;
    }

    public void turnOn(){
        Manager.getInstance().resaltarCmd(on, true, colorLetraOn);
        Manager.getInstance().noResaltarCmd(off);
    }
    
    public void turnOff(){
        Manager.getInstance().resaltarCmd(off, true, colorLetraOff);
        Manager.getInstance().noResaltarCmd(on);
    }
    
    public void unknownState(){
        Manager.getInstance().noResaltarCmd(off);
        Manager.getInstance().noResaltarCmd(on);
    }
}
