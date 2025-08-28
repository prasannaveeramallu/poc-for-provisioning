package com.example.poc.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProvisioningUpdate {
    private String resourceName;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String resourceType;
    private String jobId;
    
    public static ProvisioningUpdate of(String resourceName, String status, String message, String resourceType, String jobId) {
        ProvisioningUpdate update = new ProvisioningUpdate();
        update.setResourceName(resourceName);
        update.setStatus(status);
        update.setMessage(message);
        update.setTimestamp(LocalDateTime.now());
        update.setResourceType(resourceType);
        update.setJobId(jobId);
        return update;
    }
}
