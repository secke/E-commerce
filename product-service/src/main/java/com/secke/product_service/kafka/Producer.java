package com.secke.product_service.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secke.product_service.Model.ProductEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String PRODUCT_CREATION_TOPIC = "product-creation";

    @Autowired
    public Producer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNewProductEvent(ProductEvent product) {
        try {
            // Convert the User object to a JSON string
            String userJson = objectMapper.writeValueAsString(product);
            kafkaTemplate.send(PRODUCT_CREATION_TOPIC, userJson);
            System.out.println("Product event sent: " + userJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Failed to serialize product object: " + e.getMessage());
        }
    }
}

