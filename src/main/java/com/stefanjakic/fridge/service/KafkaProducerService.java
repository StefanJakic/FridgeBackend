package com.stefanjakic.fridge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String msg) {
        kafkaTemplate.send("statistic-topic", msg);
    }

    public void sendEmailNotification(String email, String message) {
        String emailMessage = String.format("{\"email\":\"%s\",\"message\":\"%s\"}", email, message);
        kafkaTemplate.send("email-topic", emailMessage);
    }
} 