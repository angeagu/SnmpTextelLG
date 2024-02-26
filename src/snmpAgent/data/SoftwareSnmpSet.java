package snmpAgent.data;

public class SoftwareSnmpSet extends ConditionChecker {

    private final String notificationOid;
    
    private final String leafNodeValue;

    public SoftwareSnmpSet(String notificationOid, String leafNodeValue, int condition, int bound) {
        super(condition, bound);
        this.notificationOid = notificationOid;
        this.leafNodeValue = leafNodeValue;
    }

    public String getNotificationOid() {
        return notificationOid;
    }

    public String getLeafNodeValue() {
        return leafNodeValue;
    }

}
