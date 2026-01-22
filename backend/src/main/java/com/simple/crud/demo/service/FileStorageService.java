package com.simple.crud.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${app.uploads.max-image-size:5242880}")
    private long maxImageSizeBytes;

    private Path root;

    @PostConstruct
    public void init() throws IOException {
        log.info("Initializing file storage service - uploadsDir: {}", uploadsDir);
        root = Paths.get(uploadsDir).toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            log.info("Creating uploads directory: {}", root);
            Files.createDirectories(root);
            log.info("Uploads directory created successfully: {}", root);
        } else {
            log.info("Uploads directory already exists: {}", root);
        }
        log.info("File storage service initialized - root: {}, maxSize: {} bytes", root, maxImageSizeBytes);
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        if (file.getSize() > maxImageSizeBytes) {
            log.error("File upload rejected - size exceeds limit - filename: '{}', size: {} bytes, limit: {} bytes", 
                    file.getOriginalFilename(), file.getSize(), maxImageSizeBytes);
            throw new IOException("File too large. Max 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            log.error("File upload rejected - invalid content type - filename: '{}', contentType: '{}'", 
                    file.getOriginalFilename(), contentType);
            throw new IOException("Invalid file type. Only images allowed");
        }
        
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + ext;
        Path target = root.resolve(filename);
        
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String publicPath = "/uploads/" + filename;
            // Return public path served by WebMvcConfig
            return publicPath;
        } catch (IOException ex) {
            log.error("Failed to store image file - originalFilename: '{}', targetPath: '{}', error: {}", 
                    original, target, ex.getMessage(), ex);
            throw ex;
        }
    }
}
