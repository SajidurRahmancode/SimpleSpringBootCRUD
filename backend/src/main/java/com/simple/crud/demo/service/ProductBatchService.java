package com.simple.crud.demo.service; 
import com.simple.crud.demo.model.dto.BatchUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service // Marks this class as a Spring service
@RequiredArgsConstructor // Generates constructor for final fields
@Slf4j // Provides a logger named 'log'

// Service layer for handling product batch uploads and job status
// Coordinates file saving, job launching, and status retrieval
// Returns detailed DTOs for upload results and job status
// with error handling and logging
public class ProductBatchService { // Coordinates CSV upload and batch execution

    private final JobLauncher jobLauncher; // Triggers batch jobs
    private final Job importProductJob; // Batch job to import products
    private final JobExplorer jobExplorer; // Query job executions/status

    // Handles CSV upload and starts the batch job
    // Returns a detailed DTO with job execution info
    // on success or failure
    @PreAuthorize("isAuthenticated()") // Only authenticated users can upload
    public BatchUploadResponseDto uploadAndProcessCsv(MultipartFile file) { // Handles upload + job start
        validateCsvFile(file); // Basic validation of CSV
        try { // Wrap job start in try-catch
            String filePath = saveUploadedFile(file); // Persist upload to disk
            Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // Current user
            String uploadedBy = auth != null ? auth.getName() : "anonymous"; // Username fallback

            JobParameters params = new JobParametersBuilder() // Build job parameters
                    .addString("filePath", filePath) // Path for reader
                    .addString("uploadedBy", uploadedBy) // Who uploaded (optional use)
                    .addLong("timestamp", System.currentTimeMillis()) // Ensure uniqueness
                    .toJobParameters(); // Finalize params

            JobExecution jobExecution = jobLauncher.run(importProductJob, params); // Launch job
            return buildResponse(jobExecution); // Convert execution to DTO
        } catch (Exception e) { // On failure
            log.error("Failed to start batch job", e); // Log error
            return BatchUploadResponseDto.builder() // Build failure response
                    .jobExecutionId(null) // No execution ID
                    .status("FAILED") // Mark failed
                    .message("Batch job failed to start") // Message in case of failure
                    .errors(List.of(e.getMessage())) // Include error details
                    .build(); // Return DTO
        }
    }
    // Retrieves the status of a batch job by its execution ID
    // Returns a detailed DTO with counts and status
    // If not found, returns a NOT_FOUND status in the DTO
    @PreAuthorize("isAuthenticated()") // Only authenticated users can query status
    public BatchUploadResponseDto getJobStatus(Long jobExecutionId) { // Returns job status DTO
        JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId); // Lookup execution
        if (jobExecution == null) { // If not found
            return BatchUploadResponseDto.builder() // Build not-found response
                    .jobExecutionId(jobExecutionId) // Echo ID
                    .status("NOT_FOUND") // Status marker
                    .message("Job execution not found") // Message
                    .build(); // Return DTO
        }
        return buildResponse(jobExecution); // Build populated response
    }

    private void validateCsvFile(MultipartFile file) { // Basic safety checks
        if (file == null || file.isEmpty()) { // No content
            throw new IllegalArgumentException("File is empty"); // Fail fast
        }
        String filename = file.getOriginalFilename(); // Original file name
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) { // Must be .csv
            throw new IllegalArgumentException("Only .csv files are allowed"); // Fail fast
        }
    }
    // Saves uploaded file to disk securely and efficiently
    // Returns the absolute file path as a String
    // Handles directory creation, unique naming, and streaming
    // Throws IOException on failure
    private String saveUploadedFile(MultipartFile file) throws IOException {
            // 1. Define upload directory
            Path uploadDir = Paths.get("uploads", "batch");
            Files.createDirectories(uploadDir);
            
            // 2. Generate unique filename with original extension
            String originalFilename = file.getOriginalFilename();
            String extension = ".csv"; // Default
            
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                // Validate extension is safe (optional)
                if (!extension.matches("\\.(csv|txt|data)$")) {
                    extension = ".csv"; // Force CSV if not safe
                }
            }
            
            // 3. Create unique name
            String uniqueName = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(uniqueName).normalize();
            
            // 4. Security check: ensure path stays within upload directory
            if (!filePath.startsWith(uploadDir.normalize())) {
                throw new SecurityException("Invalid file path");
            }
            
            // 5. Stream file to disk (memory efficient)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }
            
            // 6. Log for audit trail
            log.info("Saved uploaded file: {} ({} bytes)", 
                    filePath, Files.size(filePath));
            
            return filePath.toString();
    }
    // Helper to build response DTO from JobExecution
    // Used by both upload and status methods, to avoid duplication
    // Populates all relevant fields based on execution data
    // including counts and error messages
    private BatchUploadResponseDto buildResponse(JobExecution jobExecution) { // Convert JobExecution to DTO
        BatchUploadResponseDto.BatchUploadResponseDtoBuilder builder = BatchUploadResponseDto.builder() // Start builder
                .jobExecutionId(jobExecution.getId()) // Set ID
                .status(jobExecution.getStatus().name()); // Set status

        if (jobExecution.getStartTime() != null) { // Start time present
            builder.startTime(jobExecution.getStartTime()); // Add to DTO
        }
        if (jobExecution.getEndTime() != null) { // End time present
            builder.endTime(jobExecution.getEndTime()); // Add to DTO
        }

        int totalRead = 0; // Accumulator for read count
        int totalWrite = 0; // Accumulator for write count
        int totalSkip = 0; // Accumulator for skip count
        for (StepExecution se : jobExecution.getStepExecutions()) { // Iterate step executions
            totalRead += se.getReadCount(); // Sum reads
            totalWrite += se.getWriteCount(); // Sum writes
            totalSkip += se.getSkipCount(); // Sum skips
        }

        int failureCount = Math.max(0, totalRead - totalWrite); // Compute failures

        builder.totalRecords(totalRead) // Populate totals
                .successCount(totalWrite)
                .failureCount(failureCount)
                .skipCount(totalSkip);

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) { // Completed
            builder.message("Batch processing completed"); // Message
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) { // Failed
            builder.message("Batch processing failed"); // Message
        } else { // In progress
            builder.message("Batch processing in progress"); // Message
        }

        List<String> errors = new ArrayList<>(); // Collect failure exceptions
        jobExecution.getAllFailureExceptions().forEach(ex -> errors.add(ex.getMessage())); // Add messages
        if (!errors.isEmpty()) { // If any errors
            builder.errors(errors); // Include in DTO
        }

        return builder.build(); // Build final DTO
    }
}
