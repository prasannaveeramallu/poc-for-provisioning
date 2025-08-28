package com.example.poc.controller;

import com.example.poc.model.ProvisioningRequest;
import com.example.poc.model.*;
import com.example.poc.repository.JobRepository;
import com.example.poc.service.SqsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/provision")
public class ProvisioningController {
    
    private final JobRepository jobRepository;
    private final SqsService sqsService;
    
    public ProvisioningController(JobRepository jobRepository, SqsService sqsService) {
        this.jobRepository = jobRepository;
        this.sqsService = sqsService;
    }
    
    @PostMapping
    public ResponseEntity<JobResponse> provisionResources(@RequestBody ProvisioningRequest request) {
        try {
            // Generate jobId
            String jobId = UUID.randomUUID().toString();
            
            // Create and persist job
            Job job = Job.builder()
                .jobId(jobId)
                .status("PENDING")
                .currentStep("INITIALIZING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .request(request)
                .build();
                
            jobRepository.save(job);
            
            // Enqueue message
            ProvisioningMessage message = new ProvisioningMessage(jobId, request);
            sqsService.sendMessage(message);
            
            // Return 202 Accepted
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new JobResponse(jobId, "PENDING"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JobResponse(null, "ERROR"));
        }
    }
    
    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobStatus(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }
}
