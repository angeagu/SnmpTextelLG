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
public class DirectHWCommand extends CommandForHW {

    private final String directCommandResponse;

    private final String directCommand;


    public DirectHWCommand(String faceId, String cardId, String type, String directCommand, String directCommandResponse) {
        super(faceId, cardId, type);
        this.directCommand = directCommand;
        this.directCommandResponse = directCommandResponse;
    }

    @Override
    /* Este método debe devolver el comando completo a ejecutar */
    public String getFullCommandByLeafNodeValue(String value) {
        return directCommand + value;
    }

    @Override
    /* Este método debe devolver sólo el nombre del commando (se utiliza para la respuesta del comando) */
    public String getCommandIdByLeafNodeValue(String value) {
        return directCommandResponse;
    }

}
