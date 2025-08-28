package com.example.poc.model;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ProvisioningRequest {
    private String requestId;  // Overall request ID
    private List<ResourceRequest> resources;  // List of resources to provision
    private String resourceName;  // Name for the resource
    private String resourceType;  // Type of the resource (e.g., VM, EKS)
    
    // Common configuration that can be shared across resources
    private String awsRegion;
    private String azureRegion;
    private String resourceGroupName;  // For Azure resources
    
    // VM-specific fields
    private String availabilitySetName;
    private String publicIpName;
    private String vnetName;
    private String subnetName;
    private String nicName;
    private String vmName;
    private String adminUsername;
    private String adminPassword;
    
    // Constructor to initialize requestId
    public ProvisioningRequest() {
        this.requestId = UUID.randomUUID().toString();
    }
    
    // Common configuration
    private Map<String, Object> commonConfig;
}
