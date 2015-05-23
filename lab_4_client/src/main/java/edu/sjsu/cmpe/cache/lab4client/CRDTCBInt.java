package edu.sjsu.cmpe.cache.lab4client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

public interface CRDTCBInt {

    void putFinished (HttpResponse<JsonNode> resp, String servUrl);
    void getFinished (HttpResponse<JsonNode> resp, String servUrl);

    void putFailure (Exception e);
    void getFailure (Exception e);
}