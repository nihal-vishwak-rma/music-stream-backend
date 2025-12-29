package com.nihal.Music.services.Impl;

import com.nihal.Music.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    @Value("${spring.brevo.api.key}")
    private String apiKey;
    @Value("${spring.brevo.from.email}")
    private String senderEmail;
    @Value("${spring.brevo.from.name}")
    private String senderName;

    @Override
    public void sendOtpEmail(String toEmail, String subject, String otp) {


        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);

        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);

        body.put("sender", sender);
        body.put("to", new Map[]{to});
        body.put("subject", subject);
        body.put("htmlContent", "<p>" + otp + "</p>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
            System.out.println("Email Status Code: " + response.getStatusCode());
            System.out.println("Email Body: " + response.getBody());

            log.info("otp sending successfully on this email : {} ", toEmail);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("error while sending otp on this email : {} ", toEmail);
        }
    }
}
