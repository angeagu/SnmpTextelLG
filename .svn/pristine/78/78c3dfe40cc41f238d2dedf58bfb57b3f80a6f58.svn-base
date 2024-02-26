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


/* Esta clase la utilizamos principalmente para relacionar
   el formato de respuesta con la respuesta de una query.
   Es decir, nosotros recibimos una respuesta y no sabemos
   a qué query pertenece, por ello utilizamos el primer
   carácter del query para identificarlo */

public final class QueryIdentifier {
    
    private final String cardId;
    private final String faceId;
    private final String firstCharOfQuery;

    public QueryIdentifier(String faceId, String cardId, String firstCharOfQuery) {
        this.faceId = faceId;
        this.cardId = cardId;
        this.firstCharOfQuery = firstCharOfQuery;
    }

    public String getCardId() {
        return cardId;
    }

    public String getFaceId() {
        return faceId;
    }

    public String getFirstCharOfQuery() {
        return firstCharOfQuery;
    }

    /* Estos dos metodos han sido creados por netbeans porque la
       clase es immutable (final class). Esto lo hacemos para que
       pueda ser usada como key en un HashMap */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.cardId);
        hash = 97 * hash + Objects.hashCode(this.faceId);
        hash = 97 * hash + Objects.hashCode(this.firstCharOfQuery);
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
        final QueryIdentifier other = (QueryIdentifier) obj;
        if (!Objects.equals(this.cardId, other.cardId)) {
            return false;
        }
        if (!Objects.equals(this.faceId, other.faceId)) {
            return false;
        }
        if (!Objects.equals(this.firstCharOfQuery, other.firstCharOfQuery)) {
            return false;
        }
        return true;
    }
}
