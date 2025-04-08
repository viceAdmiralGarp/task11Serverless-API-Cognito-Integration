package com.task12.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Response {
    public static Map<String, Object> generateResponse(int statusCode, Object body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("body", body instanceof String ? body : toJson(body));
          Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "*");
        headers.put("Accept-Version", "*");

        response.put("headers", headers);
        return response;
    }

    private static String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON: " + e.getMessage());
        }
    }
}
