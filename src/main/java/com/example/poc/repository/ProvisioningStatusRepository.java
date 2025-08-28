package com.example.poc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.poc.model.ProvisioningStatus;

@Repository
public interface ProvisioningStatusRepository extends MongoRepository<ProvisioningStatus, String> {
    Optional<ProvisioningStatus> findByResourceName(String resourceName);
    List<ProvisioningStatus> findByRequestId(String requestId);
}
