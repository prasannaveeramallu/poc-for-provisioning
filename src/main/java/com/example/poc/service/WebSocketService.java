package com.example.poc.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.example.poc.model.ProvisioningUpdate;
import java.time.LocalDateTime;

@Service
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void sendProvisioningUpdate(ProvisioningUpdate update) {
        messagingTemplate.convertAndSend("/topic/provisioning/" + update.getResourceName(), update);
    }
    
    public void sendStatusUpdate(String jobId, String status, String message) {
        ProvisioningUpdate update = new ProvisioningUpdate();
        update.setJobId(jobId);
        update.setStatus(status);
        update.setMessage(message);
        update.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/status/" + jobId, update);
    }
}
