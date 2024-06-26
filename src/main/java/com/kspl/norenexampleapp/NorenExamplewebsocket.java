/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.kspl.norenexampleapp;

import com.noren.javaapi.NorenApiJava;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;
import com.kspl.norenexampleapp.ExampleCallback;

/**
 *
 * @author itsku
 */
public class NorenExamplewebsocket{

    public static void main(String[] args) {
        System.out.println("Hello and Welcome to Noren!");
        NorenApiJava api = new com.noren.javaapi.NorenApiJava("http://kumra.kambala.co.in:9959/NorenWClient/","ws://kumra.kambala.co.in:9657/NorenWS/");
        
        String response = api.login("NIKHESHP", "Zxcv@123", "06-05-1999", "NIKHESHP", "test123api", "java-");
        System.out.println(response);
            
        ExampleCallback appcallback=new ExampleCallback();    
        api.startwebsocket(appcallback);
        api.subscribe("NSE|22");
        try {
                Thread.sleep(10000);  // Sleep for 2 seconds
            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted: " + e.getMessage());
                e.printStackTrace();
            }
        api.unsubscribe("NSE|22");
        while(true){
            try {
            Thread.sleep(2000); // sleep for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }        
    }      
}
