package com.example.poc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProvisioningController {
    
    private final ProvisioningService provisioningService;

    public ProvisioningController(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @PostMapping("/provision/resource")
    public ResponseEntity<ProvisioningStatus> provisionResource(@RequestBody ProvisioningRequest request) {
        try {
            ProvisioningStatus status = provisioningService.provisionResource(request);
            return ResponseEntity.accepted().body(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/provision/status/{resourceName}")
    public ResponseEntity<ProvisioningStatus> getProvisioningStatus(@PathVariable String resourceName) {
        return provisioningService.getProvisioningStatus(resourceName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
