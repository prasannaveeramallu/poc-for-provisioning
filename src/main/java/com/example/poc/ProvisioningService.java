package com.example.poc;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.*;
import software.amazon.awssdk.regions.Region;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class ProvisioningService {

    private final ProvisioningStatusRepository statusRepository;

    public ProvisioningService(ProvisioningStatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public Optional<ProvisioningStatus> getProvisioningStatus(String resourceName) {
        return statusRepository.findByResourceName(resourceName);
    }

    public ProvisioningStatus provisionResource(ProvisioningRequest request) {
        ProvisioningStatus status = new ProvisioningStatus();
        status.setResourceName(request.getResourceName());
        status.setResourceType(request.getResourceType());
        status.setStartTime(LocalDateTime.now());
        status.setStatus("IN_PROGRESS");
        status.setConfiguration(request);
        
        status = statusRepository.save(status);

        try {
            if (request.getResourceType().toLowerCase().contains("eks")) {
                provisionEksService(request);
            } else if (request.getResourceType().toLowerCase().contains("vm")) {
                provisionVm(request);
            } else {
                throw new IllegalArgumentException("Unsupported resource type: " + request.getResourceType());
            }

            status.setStatus("COMPLETED");
            status.setEndTime(LocalDateTime.now());
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setEndTime(LocalDateTime.now());
            status.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            status = statusRepository.save(status);
        }
        return status;
    }



    private void provisionEksService(ProvisioningRequest request) {
        // Set up AWS credentials from environment variables
        String awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        
        if (awsAccessKey == null || awsSecretKey == null) {
            throw new IllegalStateException("AWS credentials not found in environment variables");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
            awsAccessKey,
            awsSecretKey
        );

        // Create EKS client
        EksClient eksClient = EksClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .region(Region.of(request.getAwsRegion()))
            .build();

        // Create EKS cluster
        CreateClusterResponse createClusterResponse = eksClient.createCluster(
            CreateClusterRequest.builder()
                .name(request.getEksClusterName())
                .roleArn(request.getEksRoleArn())
                .resourcesVpcConfig(VpcConfigRequest.builder()
                    .subnetIds(request.getEksSubnetIds())
                    .build())
                .build()
        );

        // Create node group
        CreateNodegroupResponse createNodegroupResponse = eksClient.createNodegroup(
            CreateNodegroupRequest.builder()
                .clusterName(request.getEksClusterName())
                .nodegroupName(request.getEksNodegroupName())
                .nodeRole(request.getEksNodeRole())
                .subnets(request.getEksSubnetIds())
                .build()
        );

        // Create EC2 client
        Ec2Client ec2Client = Ec2Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .region(Region.of(request.getAwsRegion()))
            .build();

        // Create security group
        CreateSecurityGroupResponse createSecurityGroupResponse = ec2Client.createSecurityGroup(
            CreateSecurityGroupRequest.builder()
                .groupName(request.getEksSecurityGroupName())
                .description("Security group for EKS cluster")
                .vpcId(request.getEksVpcId())
                .build()
        );

        // Get VPC details
        DescribeVpcsResponse describeVpcsResponse = ec2Client.describeVpcs(
            DescribeVpcsRequest.builder()
                .vpcIds(request.getEksVpcId())
                .build()
        );
    }

    private void provisionVm(ProvisioningRequest request) {
        // Set up Azure credentials
        // Azure credentials will be loaded from environment variables:
        // AZURE_TENANT_ID
        // AZURE_CLIENT_ID
        // AZURE_CLIENT_SECRET
        // AZURE_SUBSCRIPTION_ID
        TokenCredential credential = new EnvironmentCredentialBuilder()
            .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
            .build();

        String azureSubscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");
        if (azureSubscriptionId == null) {
            throw new IllegalStateException("Azure subscription ID not found in environment variables");
        }

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        // Create Azure Resource Manager with logging configuration
        AzureResourceManager azure = AzureResourceManager.configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();

        try {
            // Create resource group if it doesn't exist
            azure.resourceGroups()
                .define(request.getResourceGroupName())
                .withRegion(request.getAzureRegion())
                .create();

            System.out.println("Creating availability set...");
            AvailabilitySet availabilitySet = azure.availabilitySets()
                .define(request.getAvailabilitySetName())
                .withRegion(request.getAzureRegion())
                .withExistingResourceGroup(request.getResourceGroupName())
                .create();

            // Create public IP address
            PublicIpAddress publicIPAddress = azure.publicIpAddresses()
                .define(request.getPublicIpName())
                .withRegion(request.getAzureRegion())
                .withExistingResourceGroup(request.getResourceGroupName())
                .withStaticIP()
                .create();

            // Create virtual network and subnet
            Network network = azure.networks()
                .define(request.getVnetName())
                .withRegion(request.getAzureRegion())
                .withExistingResourceGroup(request.getResourceGroupName())
                .withAddressSpace("10.0.0.0/16")
                .withSubnet(request.getSubnetName(), "10.0.0.0/24")
                .create();

            Subnet subnet = network.subnets().get(request.getSubnetName());

            // Create network interface
            NetworkInterface networkInterface = azure.networkInterfaces()
                .define(request.getNicName())
                .withRegion(request.getAzureRegion())
                .withExistingResourceGroup(request.getResourceGroupName())
                .withExistingPrimaryNetwork(network)
                .withSubnet(request.getSubnetName())
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIPAddress)
                .create();

            // Create virtual machine
            azure.virtualMachines()
                .define(request.getVmName())
                .withRegion(request.getAzureRegion())
                .withExistingResourceGroup(request.getResourceGroupName())
                .withExistingPrimaryNetworkInterface(networkInterface)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername(request.getAdminUsername())
                .withRootPassword(request.getAdminPassword())
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                .create();

        } catch (Exception e) {
            // Handle exceptions appropriately
            throw new RuntimeException("Failed to provision Azure resources", e);
        }
    }
}