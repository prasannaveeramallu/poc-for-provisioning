package com.example.poc.service;

import org.springframework.stereotype.Service;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import com.example.poc.model.ProvisioningMessage;
import com.example.poc.model.Job;
import com.example.poc.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SqsListenerService {
    private final ResourceProvisioningService resourceProvisioningService;
    private final JobRepository jobRepository;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public SqsListenerService(ResourceProvisioningService resourceProvisioningService,
                            JobRepository jobRepository,
                            WebSocketService webSocketService,
                            ObjectMapper objectMapper) {
        this.resourceProvisioningService = resourceProvisioningService;
        this.jobRepository = jobRepository;
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${aws.sqs.queue-url}")
    public void receiveMessage(String message) {
        try {
            ProvisioningMessage provisioningMessage = objectMapper.readValue(message, ProvisioningMessage.class);
            String jobId = provisioningMessage.getJobId();
            log.info("Received message for job: {}", jobId);

            // Update job status to PROCESSING
            jobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("PROCESSING");
                job.setCurrentStep("STARTED_PROVISIONING");
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);
                webSocketService.sendStatusUpdate(jobId, "PROCESSING", "Started resource provisioning");
            });

            // Process the provisioning request
            resourceProvisioningService.provisionResourceAsync(
                provisioningMessage.getRequest().getResources().get(0),
                provisioningMessage.getRequest().getCommonConfig())
                .thenAccept(status -> {
                    // Update job status on completion
                    jobRepository.findById(jobId).ifPresent(job -> {
                        job.setStatus(status.getStatus());
                        job.setCurrentStep(status.getCurrentStep());
                        job.setErrorMessage(status.getErrorMessage());
                        job.setUpdatedAt(LocalDateTime.now());
                        jobRepository.save(job);
                        webSocketService.sendStatusUpdate(jobId, status.getStatus(), status.getCurrentStep());
                    });
                })
                .exceptionally(throwable -> {
                    log.error("Error processing job {}: {}", jobId, throwable.getMessage());
                    jobRepository.findById(jobId).ifPresent(job -> {
                        job.setStatus("FAILED");
                        job.setCurrentStep("ERROR");
                        job.setErrorMessage(throwable.getMessage());
                        job.setUpdatedAt(LocalDateTime.now());
                        jobRepository.save(job);
                        webSocketService.sendStatusUpdate(jobId, "FAILED", throwable.getMessage());
                    });
                    return null;
                });
        } catch (Exception e) {
            log.error("Error processing SQS message: {}", e.getMessage());
        }
    }
}
