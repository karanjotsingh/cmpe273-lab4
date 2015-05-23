package edu.sjsu.cmpe.cache.lab4client;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.*;
import java.lang.InterruptedException;
import java.io.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;


public class CRDTClnt implements CRDTCBInt {

    private ConcurrentHashMap<String, CacheServiceInterface> serverMap;
    private ArrayList<String> successServerList;
    private ConcurrentHashMap<String, ArrayList<String>> resultList;

    private static CountDownLatch countDownLatch;

    public CRDTClnt() {

        serverMap = new ConcurrentHashMap<String, CacheServiceInterface>(3);
        CacheServiceInterface cache_0 = new DistributedCacheService("http://localhost:3000", this);
        CacheServiceInterface cache_1 = new DistributedCacheService("http://localhost:3001", this);
        CacheServiceInterface cache_2 = new DistributedCacheService("http://localhost:3002", this);
        serverMap.put("http://localhost:3000", cache_0);
        serverMap.put("http://localhost:3001", cache_1);
        serverMap.put("http://localhost:3002", cache_2);
    }

    // Callbacks
    
    @Override
    public void getFailure(Exception e) {
        System.out.println("The request has failed");
        countDownLatch.countDown();
    }

    @Override
    public void getFinished(HttpResponse<JsonNode> response, String serverUrl) {

        String value = null;
        if (response != null && response.getCode() == 200) {
            value = response.getBody().getObject().getString("value");
                System.out.println("value from server " + serverUrl + "is " + value);
            ArrayList serversWithValue = resultList.get(value);
            if (serversWithValue == null) {
                serversWithValue = new ArrayList(3);
            }
            serversWithValue.add(serverUrl);

            resultList.put(value, serversWithValue);
        }

        countDownLatch.countDown();
    }
    
    @Override
    public void putFailure(Exception e) {
        System.out.println("The request has failed");
        countDownLatch.countDown();
    }

    @Override
    public void putFinished(HttpResponse<JsonNode> response, String serverUrl) {
        int code = response.getCode();
        System.out.println("completed the put response! code " + code + " on server " + serverUrl);
        successServerList.add(serverUrl);
        countDownLatch.countDown();
    }

    public boolean put(long key, String value) throws InterruptedException {
        successServerList = new ArrayList(serverMap.size());
        countDownLatch = new CountDownLatch(serverMap.size());

        for (CacheServiceInterface cache : serverMap.values()) {
            cache.put(key, value);
        }

        countDownLatch.await();

        boolean isSuccess = Math.round((float)successServerList.size() / serverMap.size()) == 1;

        if (! isSuccess) {
            delete(key, value);
        }
        return isSuccess;
    }

    public void delete(long key, String value) {

        for (final String serverUrl : successServerList) {
            CacheServiceInterface server = serverMap.get(serverUrl);
            server.delete(key);
        }
    }


    public String get(long key) throws InterruptedException {
        resultList = new ConcurrentHashMap<String, ArrayList<String>>();
        countDownLatch = new CountDownLatch(serverMap.size());

        for (final CacheServiceInterface server : serverMap.values()) {
            server.get(key);
        }
        countDownLatch.await();

       
        String rightValue = resultList.keys().nextElement();

       
        if (resultList.keySet().size() > 1 || resultList.get(rightValue).size() != serverMap.size()) {
       
            ArrayList<String> maxValues = maxKeyForTable(resultList);

            if (maxValues.size() == 1) {

                rightValue = maxValues.get(0);

                ArrayList<String> repairServers = new ArrayList(serverMap.keySet());
                repairServers.removeAll(resultList.get(rightValue));


                for (String serverUrl : repairServers) {
                    
                    System.out.println("repairing: " + serverUrl + " value: " + rightValue);
                    CacheServiceInterface server = serverMap.get(serverUrl);
                    server.put(key, rightValue);

                }

            } else {
 
            }
        }
        return rightValue;
    }
    public ArrayList<String> maxKeyForTable(ConcurrentHashMap<String, ArrayList<String>> table) {
        ArrayList<String> maxKeys= new ArrayList<String>();
        int maxValue = -1;
        for(Map.Entry<String, ArrayList<String>> entry : table.entrySet()) {
            if(entry.getValue().size() > maxValue) {
                maxKeys.clear();
                maxKeys.add(entry.getKey());
                maxValue = entry.getValue().size();
            }
            else if(entry.getValue().size() == maxValue)
            {
                maxKeys.add(entry.getKey());
            }
        }
        return maxKeys;
    }
}