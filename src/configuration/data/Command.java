/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

/**
 *
 * @author Tecnovision
 */
public class Command {

    public final static String HARDWARE = "HARDWARE";
    public final static String MEMORY = "MEMORY";
    public final static String XML = "XML";
    private final String type;

    public Command(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
