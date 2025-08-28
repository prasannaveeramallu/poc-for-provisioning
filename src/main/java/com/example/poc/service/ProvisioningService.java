package com.example.poc.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import java.util.*;

import com.azure.storage.blob.BlobServiceClient;

import com.example.poc.model.*;
import com.example.poc.repository.ProvisioningStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class ProvisioningService {
    private final ProvisioningStatusRepository statusRepository;
    private final S3Client s3Client;
    private final BlobServiceClient blobServiceClient;

    public ProvisioningService(
        ProvisioningStatusRepository statusRepository,
        S3Client s3Client,
        BlobServiceClient blobServiceClient) {
        this.statusRepository = statusRepository;
        this.s3Client = s3Client;
        this.blobServiceClient = blobServiceClient;
    }

    public ProvisioningStatus provisionSingleResource(ResourceRequest resource, Map<String, Object> commonConfig) {
        ProvisioningStatus status = ProvisioningStatus.builder()
                .jobId(resource.getJobId())
                .status("IN_PROGRESS")
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType())
                .currentStep("INITIALIZING")
                .build();

        status = statusRepository.save(status);

        try {
            String resourceType = resource.getResourceType().toUpperCase();
            switch (resourceType) {
                case "S3":
                    provisionS3Bucket(createProvisioningRequest(resource, commonConfig));
                    break;
                case "BLOB":
                    provisionAzureBlob(createProvisioningRequest(resource, commonConfig));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
            }

            status.setStatus("COMPLETED");
            status.setCurrentStep("COMPLETED");
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setCurrentStep("ERROR");
            status.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            status = statusRepository.save(status);
        }
        return status;
    }

    private ProvisioningRequest createProvisioningRequest(ResourceRequest resource, Map<String, Object> commonConfig) {
        ProvisioningRequest request = new ProvisioningRequest();
        request.setResourceName(resource.getResourceName());
        request.setResourceType(resource.getResourceType());
        request.setResources(Arrays.asList(resource));
        request.setAwsRegion((String) commonConfig.get("awsRegion"));
        request.setAzureRegion((String) commonConfig.get("azureRegion"));
        request.setResourceGroupName((String) commonConfig.get("resourceGroupName"));
        return request;
    }

    private void provisionS3Bucket(ProvisioningRequest request) {
        String awsRegion = request.getAwsRegion();
        
        // Create S3 bucket with configuration
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
            .bucket(request.getResourceName().toLowerCase())  // S3 bucket names must be lowercase
            .createBucketConfiguration(
                CreateBucketConfiguration.builder()
                    .locationConstraint(awsRegion)
                    .build())
            .build();

        s3Client.createBucket(createBucketRequest);
    }

    private void provisionAzureBlob(ProvisioningRequest request) {
        // Create the container with default access level
        String containerName = request.getResourceName().toLowerCase();
        blobServiceClient.createBlobContainer(containerName);
    }
}
