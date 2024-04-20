package com.gj.hpm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LineUtil {

    public Boolean changeRichmenu(String lineId, String richmenuId, String token) {
        try {

            RestTemplate restTemplate = new RestTemplate();

            String apiUrl = "https://api.line.me/v2/bot/user/" + lineId + "/richmenu/"
                    + richmenuId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            String body = "{}";
            restTemplate.exchange(apiUrl, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean sentMessage(String lineId, String token, String stringMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String apiUrl = "https://api.line.me/v2/bot/message/push";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> requestBodyLine = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            message.put("type", "text");
            message.put("text", stringMessage);
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);
            requestBodyLine.put("messages", messages);
            requestBodyLine.put("to", lineId);
            String jsonBody = objectMapper.writeValueAsString(requestBodyLine);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity,
                    String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
