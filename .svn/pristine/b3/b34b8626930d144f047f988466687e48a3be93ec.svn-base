package snmpAgent.data;

import java.util.ArrayList;
import java.util.Optional;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;
import static snmpAgent.data.SoftwareNotificationCondition.BOUND;
import static snmpAgent.data.SoftwareNotificationCondition.STRING_VALUE_ERROR_CONDITION;
import static snmpAgent.data.SoftwareNotificationCondition.VALUE_MUST_MATCH_TO;


public abstract class ConditionChecker {

    private static final Logger LOG = Logger.getLogger(ConditionChecker.class.getName());

    public final static int BOUND = 0;
    public final static int STRING_VALUE_ERROR_CONDITION = 1;
    public final static int VALUE_MUST_MATCH_TO = 2;
    public final static int ABOVE = 3;
    public final static int BELOW = 4;

    private ArrayList<String> valueMustMatchToOidsValue;

    private int condition;

    private int lowerBound;
    private int upperBound;
    
    private int bound;

    private String errorValue;


    public ConditionChecker(int condition, String lowerBound, String upperBound) {
        this.valueMustMatchToOidsValue = null;
        this.condition = condition;
        this.errorValue = null;

        try {
            this.lowerBound = Integer.parseInt(lowerBound);
            this.upperBound = Integer.parseInt(upperBound);

        } catch(NumberFormatException ex) {
            LOG.error("Los límites de las notificaciones software deben ser numericas.");
        }
    }

    public ConditionChecker(int condition, int bound) {
        this.valueMustMatchToOidsValue = null;
        this.condition = condition;
        this.errorValue = null;
        this.lowerBound = -1;
        this.upperBound = -1;
        this.bound = bound;
    }

    public ConditionChecker(int condition, String stringValueError) {
        this.condition = condition;
        this.lowerBound = -1;
        this.upperBound = -1;
        this.errorValue = stringValueError;
        this.valueMustMatchToOidsValue = null;
    }

    public ConditionChecker(int condition, String[] matchToValueOfOidElements) {
        this.valueMustMatchToOidsValue = new ArrayList<>();
        this.condition = condition;
        this.lowerBound = -1;
        this.upperBound = -1;
        this.errorValue = null;

        for (String element : matchToValueOfOidElements) {
            valueMustMatchToOidsValue.add(element);
        }
    }


    protected boolean check(String value, SnmpAgent agent) {
        Optional<Integer> currentIntValue = Optional.empty();
        boolean sendSwNotification = false;

        if (value != null) {
            
            switch(condition){
                case BOUND:

                    /* Cuando se pierde la comunicacion con las tarjetas, el valor
                       que se manda a todos los nodos de esa tarjeta es "" */
                    if(value.length() > 0){
                        try {
                            int currentValue = Integer.parseInt(value);
                            if ((currentValue < lowerBound) || (currentValue > upperBound)) {
                                sendSwNotification = true;
                            }

                        } catch (NumberFormatException e) {
                            LOG.error("Software condition [BOUND] and value [" + value + "]");
                        }
                    }
                    
                    break;

                case STRING_VALUE_ERROR_CONDITION:

                    if (value.equalsIgnoreCase(errorValue)) {
                        sendSwNotification = true;
                    }
                    
                    break;

                case VALUE_MUST_MATCH_TO:

                    for (String currentOid : valueMustMatchToOidsValue) {

                        if (agent.getInfoByOid(currentOid) != null) {
                            String currentValue = agent.getInfoByOid(currentOid).toString();

                            if (!value.equalsIgnoreCase(currentValue)) {
                                sendSwNotification = true;
                            }
                        }
                    }

                    break;

                case ABOVE:
                    currentIntValue = getIntValue(value);
                    sendSwNotification = currentIntValue.isPresent() && currentIntValue.get() > bound;
                    break;

                case BELOW:
                    currentIntValue = getIntValue(value);
                    sendSwNotification = currentIntValue.isPresent() && currentIntValue.get() < bound;
                    break;

                default:
                    LOG.error("Unexpected notification condition: " + condition);
            }

        // si Value es NULL estoy en error porque deberia tener esta informacion (suele pasar en el arranque)
        } else {
            sendSwNotification = true;
        }

        return sendSwNotification;
    }
    
    
    private Optional<Integer> getIntValue(String value) {
        Optional<Integer> intValue = Optional.empty();

        /* Cuando se pierde la comunicacion con las tarjetas, el valor
           que se manda a todos los nodos de esa tarjeta es "" */
        if (value.length() > 0) {
            try {
                intValue = Optional.ofNullable(Integer.parseInt(value));

            } catch (NumberFormatException e) {
                LOG.error("Software set [BOUND] and value [" + value + "]");
            }
        }

        return intValue;
    }
}
