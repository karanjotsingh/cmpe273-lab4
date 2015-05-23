package edu.sjsu.cmpe.cache.lab4client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;

public class DistributedCacheService implements CacheServiceInterface {
    private final String cacheServerUrl;

    private CRDTCBInt callback;

    public DistributedCacheService(String serverUrl) {
        this.cacheServerUrl = serverUrl;
    }
    public DistributedCacheService(String serverUrl, CRDTCBInt callbk) {
        this.cacheServerUrl = serverUrl;
        this.callback = callbk;
    }
    @Override
    public String get(long key) {
        Future<HttpResponse<JsonNode>> futVal = Unirest.get(this.cacheServerUrl + "/cache/{key}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        callback.getFailure(e);
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        callback.getFinished(response, cacheServerUrl);
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });

        return null;
    }
    
    @Override
    public void delete(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .delete(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }

        System.out.println("response is " + response);

        if (response == null || response.getCode() != 204) {
            System.out.println("Failed to delete from the cache.");
        } else 
        {
            System.out.println("Deleted " + key + " from " + this.cacheServerUrl);
        }

    }
    
    @Override
    public void put(long key, String value) {
        Future<HttpResponse<JsonNode>> futVal = Unirest.put(this.cacheServerUrl + "/cache/{key}/{value}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .routeParam("value", value)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                        callback.putFailure(e);
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        int code = response.getCode();
                        System.out.println("received code " + code);
                        callback.putFinished(response, cacheServerUrl);
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });
    }
}
