package com.example.poc;

import lombok.Data;
import java.util.List;

@Data
public class ProvisioningRequest {
    // AWS Configuration properties
    private String awsRegion;
    private String eksClusterName;
    private String eksRoleArn;
    private List<String> eksSubnetIds;
    private String eksNodegroupName;
    private String eksNodeRole;
    private String eksSecurityGroupName;
    private String eksVpcId;

    // Azure Configuration properties
    private String resourceGroupName;
    private String availabilitySetName;
    private String azureRegion;
    private String vmName;
    private String vnetName;
    private String subnetName;
    private String nicName;
    private String publicIpName;
    private String adminUsername;
    private String adminPassword;

    // Original properties
    private String resourceName;
    private String resourceType;
}