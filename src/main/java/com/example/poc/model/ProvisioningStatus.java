package com.example.poc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningStatus {
    private String jobId;
    private String status;
    private String resourceName;
    private String resourceType;
    private String errorMessage;
    private String currentStep;
    
    private String requestId;  // added to track the overall request

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
    
  
    
    
  
}