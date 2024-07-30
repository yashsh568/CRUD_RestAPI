package com.example.walletcrud.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SMSService {

    @Value("https://api.kiwiplans.com:7002/send/sms")
    private String smsApiUrl;

    private final RestTemplate restTemplate;

    public SMSService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async("smsExecutor")
    public void sendSMS(Long mobile, String message) {
        Map<String, Object> request = new HashMap<>();
        request.put("mobile", mobile);
        request.put("message", message);

        try {
            restTemplate.postForObject(smsApiUrl, request, String.class);
            // Optionally log the response or handle it as needed
        } catch (Exception e) {
            // Log the error
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }
}
