package com.example.poc.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.subscription-id}")
    private String subscriptionId;

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.resource-group}")
    private String resourceGroup;

    @Value("${azure.storage.location}")
    private String location;

    @Bean
    public BlobServiceClient blobServiceClient() {
        // Create Azure credentials using Azure AD
        var credential = new DefaultAzureCredentialBuilder()
            .build();

        // Create the storage account if it doesn't exist
        var profile = new AzureProfile(tenantId, subscriptionId, com.azure.core.management.AzureEnvironment.AZURE);
        var storageManager = StorageManager.authenticate(credential, profile);


        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(accountName)
            .withRegion(location)
            .withExistingResourceGroup(resourceGroup)
            .withSku(com.azure.resourcemanager.storage.models.StorageAccountSkuType.STANDARD_LRS)
            .create();

        // Create blob service client using Azure AD credentials
        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
        return new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
    }
}
