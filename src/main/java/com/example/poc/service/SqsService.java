package com.example.poc.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.example.poc.model.ProvisioningMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SqsService {
    
    private final AmazonSQS sqsClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;
    
    public SqsService(AmazonSQS sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }
    
    public void sendMessage(ProvisioningMessage message) {
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody);
            
            sqsClient.sendMessage(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to SQS", e);
        }
    }
}
