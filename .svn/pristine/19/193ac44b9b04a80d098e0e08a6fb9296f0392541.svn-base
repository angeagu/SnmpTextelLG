/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import java.util.Objects;

/**
 *
 * @author Tecnovision
 */


/* Esta clase la vamos a utilizar principalmente 
   para relacionar los identidicadores proporcionados
   por una respuesta de una query con su OID. En este 
   caso no nos sirve el QueryIdentifier porque en una
   misma respuesta nos pueden dar informacion de varios
   elementos hardware, por ejemplo, los ventiladore. */

/* Esta clase también se utiliza para las Hardware 
   notification donde el idOfQueryResponse en este
   caso se corresponde con la primera letra del 
   mensaje */
public final class HardwareElementIdentifier {
    
    private final String cardId;
    private final String faceId;
    private final String idOfQueryResponse;

    public HardwareElementIdentifier(String faceId, String cardId, String idOfQueryResponse) {
        this.faceId = faceId;
        this.cardId = cardId;
        this.idOfQueryResponse = idOfQueryResponse;
    }

    public String getCardId() {
        return cardId;
    }

    public String getFaceId() {
        return faceId;
    }

    public String getIdOfQueryResponse() {
        return idOfQueryResponse;
    }

    @Override
    public String toString() {
        return "HardwareElementIdentifier{" + "cardId=" + cardId + ", faceId=" + faceId + ", idOfQueryResponse=" + idOfQueryResponse + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.cardId);
        hash = 41 * hash + Objects.hashCode(this.faceId);
        hash = 41 * hash + Objects.hashCode(this.idOfQueryResponse);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HardwareElementIdentifier other = (HardwareElementIdentifier) obj;
        if (!Objects.equals(this.cardId, other.cardId)) {
            return false;
        }
        if (!Objects.equals(this.faceId, other.faceId)) {
            return false;
        }
        if (!Objects.equals(this.idOfQueryResponse, other.idOfQueryResponse)) {
            return false;
        }
        return true;
    }
}
