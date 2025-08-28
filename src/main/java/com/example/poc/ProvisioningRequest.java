package com.example.poc;

import com.example.poc.model.ResourceRequest;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProvisioningRequest {
    private String requestId;  // overall request ID
    private List<ResourceRequest> resources;  // List of resources to provision
    private String resourceName;  // name for the resource
    private String resourceType;  // type of the resource (e.g., VM, EKS)
    
    // common configuration that can be shared across resources
    private String awsRegion;
    private String azureRegion;
    private String resourceGroupName;  // for azure resources
    
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
    
    public String getResourceName() {
        return this.resourceName;
    }
    
    public String getResourceType() {
        return this.resourceType;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getAvailabilitySetName() {
        return availabilitySetName;
    }

    public void setAvailabilitySetName(String availabilitySetName) {
        this.availabilitySetName = availabilitySetName;
    }

    public String getPublicIpName() {
        return publicIpName;
    }

    public void setPublicIpName(String publicIpName) {
        this.publicIpName = publicIpName;
    }

    public String getVnetName() {
        return vnetName;
    }

    public void setVnetName(String vnetName) {
        this.vnetName = vnetName;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getNicName() {
        return nicName;
    }

    public void setNicName(String nicName) {
        this.nicName = nicName;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}