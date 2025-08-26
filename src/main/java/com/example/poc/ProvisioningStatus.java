package com.example.poc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "provisioning_status")
public class ProvisioningStatus {
    @Id
    private String id;
    private String resourceName;
    private String resourceType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private Object configuration;
}
