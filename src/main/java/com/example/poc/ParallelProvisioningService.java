package com.example.poc;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;

import com.example.poc.service.ResourceProvisioningService;
import com.example.poc.service.WebSocketService;
import com.example.poc.model.*;
import com.example.poc.repository.ProvisioningStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;

@Service
@EnableAsync
public class ParallelProvisioningService {
    
    @Autowired
    private ProvisioningStatusRepository statusRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private ResourceProvisioningService resourceProvisioningService;

    public List<String> startProvisioning(ProvisioningRequest request) {
        // Extract common configuration
        Map<String, Object> commonConfig = new HashMap<>();
        commonConfig.put("awsRegion", request.getAwsRegion());
        commonConfig.put("azureRegion", request.getAzureRegion());
        commonConfig.put("resourceGroupName", request.getResourceGroupName());

        // Start parallel provisioning for each resource
        List<CompletableFuture<ProvisioningStatus>> futures = request.getResources().stream()
            .map(resource -> resourceProvisioningService.provisionResourceAsync(resource, commonConfig))
            .collect(Collectors.toList());

        // Return the job IDs for tracking
        return request.getResources().stream()
            .map(ResourceRequest::getJobId)
            .collect(Collectors.toList());
    }

    public List<ProvisioningStatus> getStatusForRequest(String requestId) {
        // This would require adding a requestId field to ProvisioningStatus
        // and a corresponding query method in the repository
        return statusRepository.findByRequestId(requestId);
    }

    public Optional<ProvisioningStatus> getStatusForJob(String jobId) {
        return statusRepository.findById(jobId);
    }
}
