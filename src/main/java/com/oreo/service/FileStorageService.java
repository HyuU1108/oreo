package com.oreo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
         try {
            rootLocation = Paths.get(uploadDir);
            Files.createDirectories(rootLocation);
            System.out.println("File storage initialized at: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location: " + uploadDir, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilenameNullable = file.getOriginalFilename();
        if (originalFilenameNullable == null) {
            throw new RuntimeException("Uploaded file is missing its original filename.");
        }

        String originalFilename = StringUtils.cleanPath(originalFilenameNullable);
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex);
        } else {
             throw new RuntimeException("File extension is missing or invalid: " + originalFilename);
        }

        String storedFileName = UUID.randomUUID().toString() + extension;

        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(storedFileName))
                                                     .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Security violation: Cannot store file outside the designated directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("File stored successfully: " + storedFileName);
            return storedFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred storing file " + originalFilename, e);
        }
    }

    public void deleteFile(String filename) {
         if (filename == null || filename.isBlank()) {
            System.out.println("Skipping delete: filename is null or blank.");
            return;
        }

        try {
            Path fileToDelete = rootLocation.resolve(filename).normalize().toAbsolutePath();

            if (!fileToDelete.getParent().equals(this.rootLocation.toAbsolutePath())) {
                 System.err.println("Security violation: Attempt to delete file outside designated directory: " + filename);
                return;
            }

            boolean deleted = Files.deleteIfExists(fileToDelete);
            if (deleted) {
                 System.out.println("File deleted successfully: " + filename);
            } else {
                 System.out.println("File not found, delete skipped: " + filename);
            }

        } catch (IOException e) {
             System.err.println("Could not delete file: " + filename + ". Reason: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("An unexpected error occurred deleting file " + filename + ": " + e.getMessage());
        }
    }

    public static boolean isExternalUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://");
    }
}