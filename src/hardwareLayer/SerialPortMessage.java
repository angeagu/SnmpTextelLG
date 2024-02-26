/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hardwareLayer;

/**
 *
 * @author Tecnovision
 */
public class SerialPortMessage {
    
    private final String cardId;
    private final String message;
    private final String protocol;

    public SerialPortMessage(String cardId, String message, String protocol) {
        this.cardId = cardId;
        this.message = message;
        this.protocol = protocol;
    }

    public String getCardId() {
        return cardId;
    }

    public String getMessage() {
        return message;
    }
    
    public String getProtocol() {
        return protocol;
    }
}
