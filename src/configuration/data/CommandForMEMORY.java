/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import org.apache.log4j.Logger;

/**
 *
 * @author Tecnovision
 */
public class CommandForMEMORY extends Command {

    private static final Logger LOG = Logger.getLogger(CommandForMEMORY.class.getName());

    public CommandForMEMORY(String type) {
        super(type);
    }
}
