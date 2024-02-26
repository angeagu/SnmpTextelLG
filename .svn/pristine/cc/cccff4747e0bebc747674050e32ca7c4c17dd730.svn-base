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
public final class CardIdentifier {
    
    private final String cardId;
    private final String faceId;

    public CardIdentifier(String cardId, String faceId) {
        this.cardId = cardId;
        this.faceId = faceId;
    }

    /* Estos dos metodos han sido creados por netbeans porque la
       clase es immutable (final class). Esto lo hacemos para que
       pueda ser usada como key en un HashMap */

    /*    //// explicar
    @Override
    public String toString() {
        return "CardIdentifier{" + "cardId=" + cardId + ", faceId=" + faceId + '}';
    } */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.cardId);
        hash = 67 * hash + Objects.hashCode(this.faceId);
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
        final CardIdentifier other = (CardIdentifier) obj;
        if (!Objects.equals(this.cardId, other.cardId)) {
            return false;
        }
        if (!Objects.equals(this.faceId, other.faceId)) {
            return false;
        }
        return true;
    }

    public String getCardId() {
        return cardId;
    }

    public String getFaceId() {
        return faceId;
    }
    
    
}


