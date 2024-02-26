/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import configuration.Loader;
import java.util.ArrayList;

/**
 *
 * @author Tecnovision
 */
public class ScheduledQueryList {
    
    private final int period;
    private final int initialSecondsToSendQueries;
    private int secondsToSendQueries;
    private final ArrayList<QueryData> queryList;

    public ScheduledQueryList(int period, int secondsToSendQueries) {
        this.period = period;
        this.queryList = new ArrayList<>();
        this.secondsToSendQueries = secondsToSendQueries;
        this.initialSecondsToSendQueries = secondsToSendQueries;
    }


    public void addQuery(QueryData query){
        queryList.add(query);
    }
    
    
    public boolean check(){
        if (secondsToSendQueries > 0) {
            secondsToSendQueries--;

            if (secondsToSendQueries == 0) {
                secondsToSendQueries = (period > 0) ? period : secondsToSendQueries;
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<QueryData> getQueryList(){
        return queryList;
    }
    
    public void restartOnStartQueries(){
        if(period == Loader.PERIOD_DISABLED_FOR_ON_START_QUERIES){
            secondsToSendQueries = initialSecondsToSendQueries;
        }
    }
    
    public void print(){
        System.out.print("Periodo [" + period + "] secondsToSendQueries [" +
                secondsToSendQueries + " ] queries: ");
        
        for(QueryData currentQuery : queryList ){
            System.out.print(currentQuery.getQuery() + " ");
        }
        System.out.println("");
    }
}
