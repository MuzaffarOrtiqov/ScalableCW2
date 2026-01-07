package api.vybe.controller;

import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.stereotype.Service;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class AzureBlobService {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.container-name}")
    private String containerName;

    private BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        BlobServiceClient blobServiceClient = getBlobServiceClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Generate unique filename with date folder structure
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String dateFolder = "videos/" + LocalDate.now().toString();
        String blobName = dateFolder + "/" + UUID.randomUUID().toString() + fileExtension;

        // Upload to Azure
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        // Return the blob URL (without SAS token)
        return blobClient.getBlobUrl();
    }

    public String generateReadSasUrl(String blobUrl) {
        try {
            BlobServiceClient blobServiceClient = getBlobServiceClient();

            // Extract blob name from URL
            // Example URL: https://vybevideos.blob.core.windows.net/videos/videos/2025-12-25/filename.jpg
            String blobName = extractBlobNameFromUrl(blobUrl);

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Set SAS token permissions (read only)
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

            // Set SAS token expiry (1 hour from now)
            OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);

            // Generate SAS token
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);
            String sasToken = blobClient.generateSas(sasValues);

            // Return URL with SAS token
            return blobClient.getBlobUrl() + "?" + sasToken;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SAS URL", e);
        }
    }

    private String extractBlobNameFromUrl(String blobUrl) {
        try {
            // Decode the URL first to handle any encoding
            String decodedUrl = java.net.URLDecoder.decode(blobUrl, "UTF-8");

            // Extract blob name from full URL
            String[] parts = decodedUrl.split("/" + containerName + "/");
            if (parts.length > 1) {
                return parts[1];
            }
            throw new IllegalArgumentException("Invalid blob URL format");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract blob name from URL: " + blobUrl, e);
        }
    }
}

