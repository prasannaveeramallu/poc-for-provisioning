package com.example.poc.model;

import lombok.Data;
import java.util.Map;

@Data
public class ResourceRequest {
    private String resourceName;
    private String resourceType;
    private String jobId;  // will be generated when processing
    private Map<String, Object> configuration;  //this will hold resource-specific configuration
}
