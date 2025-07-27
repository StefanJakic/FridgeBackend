package com.stefanjakic.fridge.controller;

import com.stefanjakic.fridge.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducerService service;

    @PostMapping("/send")
    public String send(@RequestParam String msg) {
        service.send(msg);
        return "Sent: " + msg;
    }
} 