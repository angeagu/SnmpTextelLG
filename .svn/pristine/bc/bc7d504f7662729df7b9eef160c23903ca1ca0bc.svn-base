/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;


public class SoftwareNotificationCondition extends ConditionChecker {

    private final int retries;
    private final String notificationOid;
    private final String descriptionForLog;


    public SoftwareNotificationCondition(String lowerBound, String upperBound,
                        String notificationOid, int retries, String descriptionForLog) {

        super(BOUND, lowerBound, upperBound);
        this.descriptionForLog = descriptionForLog;
        this.retries = retries;
        this.notificationOid = notificationOid;
    }

    public SoftwareNotificationCondition(String stringValueError, String notificationOid,
            int retries, String descriptionForLog) {
        
        super(STRING_VALUE_ERROR_CONDITION, stringValueError);
        this.retries = retries;
        this.notificationOid = notificationOid;
        this.descriptionForLog = descriptionForLog;
    }

    public SoftwareNotificationCondition(String[] matchToValueOfOidElements,
                        String notificationOid, int retries, String descriptionForLog) {
        
        super(VALUE_MUST_MATCH_TO, matchToValueOfOidElements);
        this.retries = retries;
        this.notificationOid = notificationOid;
        this.descriptionForLog = descriptionForLog;
    }
    
    public String getNotificationOid() {
        return notificationOid;
    }

    public int getRetries() {
        return retries;
    }

    public String getDescriptionForLog() {
        return descriptionForLog;
    }
}
