package com.example.poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.poc.model.ProvisioningRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningMessage {
    private String jobId;
    private ProvisioningRequest request;
}
