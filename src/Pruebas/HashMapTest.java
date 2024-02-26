/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import configuration.data.ScheduledQueryKey;
import java.util.HashMap;

/**
 *
 * @author Tecnovision
 */
public class HashMapTest {
    
    public static void main(String[] args){
        
        HashMap<ScheduledQueryKey, String> map = new HashMap<>();
        
        
        
        map.put(new ScheduledQueryKey(5, 5), "consulta1");
        map.put(new ScheduledQueryKey(10, 10), "consulta2");
        
        
        String result = map.get(new ScheduledQueryKey(5, 5));
        System.out.println(result);
    }
}
