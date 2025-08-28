package com.example.poc.service;

import com.example.poc.model.ResourceRequest;
import com.example.poc.model.ProvisioningStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ResourceProvisioningService {
    
    private final ProvisioningService provisioningService;
    
    public ResourceProvisioningService(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }
    
    @Async
    public CompletableFuture<ProvisioningStatus> provisionResourceAsync(ResourceRequest resource, Map<String, Object> commonConfig) {
        // Generate unique job ID for this resource
        resource.setJobId(UUID.randomUUID().toString());
        
        return CompletableFuture.supplyAsync(() -> {
            return provisioningService.provisionSingleResource(resource, commonConfig);
        });
    }
}
