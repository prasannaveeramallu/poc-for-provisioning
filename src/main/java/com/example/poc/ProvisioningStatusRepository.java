package com.example.poc;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvisioningStatusRepository extends MongoRepository<ProvisioningStatus, String> {
    Optional<ProvisioningStatus> findByResourceName(String resourceName);
}
