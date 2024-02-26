/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Tecnovision
 */
public class TimeOperations {
    
    
    public static void main(String[] args){
        String scheduledTime = "16:59:00";
    
        
        
        Calendar currentDate = new GregorianCalendar();
        int currentHour = currentDate.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentDate.get(Calendar.MINUTE);
        int currentSecond = currentDate.get(Calendar.SECOND);
        
        
        System.out.println("Hora Actual -> " + currentHour + ":" + currentMinute + ":" + currentSecond);

        
        String[] elements = scheduledTime.split(":");
        int hour = 0, minute = 0, second = 0;
        
        if(elements.length == 3){
            hour = Integer.parseInt(elements[0]);
            minute = Integer.parseInt(elements[1]);
            second = Integer.parseInt(elements[2]);
            hour = (currentHour > hour) ? hour+24 : hour;
            
        }else if(elements.length == 2){
            hour = Integer.parseInt(elements[0]);
            minute = Integer.parseInt(elements[1]);
            hour = (currentHour > hour) ? hour+24 : hour;

        }else{
            System.out.println("ERROR");
        }
        
        
        System.out.println("Hora programada -> " + hour + ":" + minute + ":" + second);
        
        
        
        
        
        
        
        int timeToGo = ((currentHour*3600) + (currentMinute*60) + (currentSecond)) - ((hour*3600) + (minute*60) + (second));
        timeToGo = Math.abs(timeToGo);
        
        
        System.out.println(timeToGo);
    }
}
