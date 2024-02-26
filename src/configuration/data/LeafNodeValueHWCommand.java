/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import java.util.LinkedHashMap;

/**
 *
 * @author Tecnovision
 */
public class LeafNodeValueHWCommand extends CommandForHW {

    private final LinkedHashMap<String, String> commandByLeafNodeValue;


    public LeafNodeValueHWCommand(String faceId, String cardId, String type) {
        super(faceId, cardId, type);
        this.commandByLeafNodeValue = new LinkedHashMap<>();
    }

    public void addCommand(String value, String commandId){
        commandByLeafNodeValue.put(value, commandId);
    }

    @Override
    /* Este método debe devolver el comando completo a ejecutar */
    public String getFullCommandByLeafNodeValue(String value) {
        return commandByLeafNodeValue.get(value);
    }

    @Override
    /* Este método debe devolver sólo el nombre del commando (se utiliza para la respuesta del comando) */
    public String getCommandIdByLeafNodeValue(String value) {
        return commandByLeafNodeValue.get(value);
    }
}
