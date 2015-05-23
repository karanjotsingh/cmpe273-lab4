package edu.sjsu.cmpe.cache.lab4client;

import com.mashape.unirest.http.Unirest;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Lab4Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Cache Client...");
        CRDTClnt crdtClient = new CRDTClnt();

// STEP 1
        boolean result = crdtClient.put(1, "a");
        System.out.println("result is " + result);
        System.out.println("put(1 => a); sleeping 30s :::Step 1:::");
        Thread.sleep(30*1000);
        


// STEP 2
        crdtClient.put(1, "b");
        System.out.println("put(1 => b); sleeping 30s :::Step 2:::");
        Thread.sleep(30*1000);
        

// STEP 3
        String value = crdtClient.get(1);
        System.out.println("get(1) => " + value+":::Step 3:::");

        System.out.println("goodbye");
        Unirest.shutdown();
    }

}
