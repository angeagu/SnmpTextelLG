/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import configuration.Configuration;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/**
 *
 * @author Tecnovision
 */
public class QueryData {

    private String query;
    private final ArrayList<String> oidList;
    private static final Logger LOG = Logger.getLogger(QueryData.class.getName());
    
    
    public QueryData(String query) {
        this.query = (query != null) ? query : "";
        this.oidList = null;
    }


    public QueryData(String query, ArrayList<String> oidList) {
        this.query = (query != null) ? query : "";
        this.oidList = (oidList != null) ? oidList : new ArrayList<String>();
    }

    
    public String getQuery() {
        return query;
    }
    
    private boolean isQueryForLog(){
        return (oidList != null);
    }

    public String getQueryToSend(SnmpAgent agent){
       String queryToReturn = "";
        
       if(!this.isQueryForLog()){
           queryToReturn = query;

       }else{
            for(String currentOid : oidList){
                String currentValue = agent.getInfoByOid(currentOid).toString();
                LOG.info("Query [" + query + "] current value [" + currentValue + "]");
           }
       }
       
       return queryToReturn;
   }
    
     public void setQueryForTextelProtocol(){
        if(query != null && !query.isEmpty() && query.indexOf(Configuration.STX)==-1){
            query = Configuration.STX + query + Configuration.ETX;
            System.out.println("query: " + query.toString());

        }
    }
     
     
      public void setQueryForLGProtocol(String cardId){
        if(query != null && !query.isEmpty()){
            query = query.replace("ID", cardId); // + Configuration.CR;////// esto deberia ser una constante!
            
            
            if(query.charAt(query.length()-1) != Configuration.CR){
                query = query + Configuration.CR;
            }
        }
    }
}
