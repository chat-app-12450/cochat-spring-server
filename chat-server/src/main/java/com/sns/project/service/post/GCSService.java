package com.sns.project.service.post;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
public class GCSService {

  @Value("${gcs.bucket-name}")
  private String bucketName;

  @Value("${gcs.credentials.file-path}")
  private String credentialsPath;

  private Storage getStorage() throws IOException {
    return StorageOptions.newBuilder()
        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credentialsPath))))
        .build()
        .getService();
  }

  public String uploadFile(byte[] fileBytes, String fileName) {
    try {
        Storage storage = getStorage();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;
        BlobId blobId = BlobId.of(bucketName, newFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(getContentType(fileName))
            .build();

        storage.create(blobInfo, fileBytes);

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, newFileName);
    } catch (IOException e) {
        throw new RuntimeException("GCS upload failed", e);
    }
  }

  private String getContentType(String fileName) {
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    switch (extension) {
        case "jpg":
        case "jpeg":
            return "image/jpeg";
        case "png":
            return "image/png";
        case "gif":
            return "image/gif";
        case "webp":
            return "image/webp";
        default:
            return "application/octet-stream";
    }
  }

}
