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
public final class ScheduledQueryKey{
    
    private final int period;
    private final int secondsToSendQueries;

    public ScheduledQueryKey(int period, int secondsToSendQueries) {
        this.period = period;
        this.secondsToSendQueries = secondsToSendQueries;
    }

    /* Estos dos metodos han sido creados por netbeans porque la
       clase es immutable (final class). Esto lo hacemos para que
       pueda ser usada como key en un HashMAp*/

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.period;
        hash = 37 * hash + this.secondsToSendQueries;
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
        final ScheduledQueryKey other = (ScheduledQueryKey) obj;
        if (this.period != other.period) {
            return false;
        }
        if (this.secondsToSendQueries != other.secondsToSendQueries) {
            return false;
        }
        return true;
    }
}
