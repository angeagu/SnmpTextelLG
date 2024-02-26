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
public abstract class CommandForHW extends Command {

    protected final String faceId;

    protected final String cardId;
    

    public CommandForHW(String faceId, String cardId, String type) {
        super(type);
        this.faceId = faceId;
        this.cardId = cardId;
    }

    public String getFaceId() {
        return faceId;
    }

    public String getCardId() {
        return cardId;
    }

    /* Este método debe devolver el comando completo a ejecutar */
    public abstract String getFullCommandByLeafNodeValue(String value);

    /* Este método debe devolver sólo el nombre del commando (se utiliza para la respuesta del comando) */
    public abstract String getCommandIdByLeafNodeValue(String value);

}
