package com.simple.crud.demo.model.dto; // DTO package for data transfer objects
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data // Lombok: generates getters/setters/toString/equals/hashCode
@Builder // Lombok: enables builder pattern for this DTO
@NoArgsConstructor // Lombok: generates no-args constructor
@AllArgsConstructor // Lombok: generates all-args constructor
public class BatchUploadResponseDto { // Response payload for batch job status
    private Long jobExecutionId; // Spring Batch execution ID
    private String status; // Current job status (STARTED/COMPLETED/FAILED)
    private LocalDateTime startTime; // Job start time
    private LocalDateTime endTime; // Job end time
    private Integer totalRecords; // Total records read
    private Integer successCount; // Records successfully written
    private Integer failureCount; // Records failed to write
    private Integer skipCount; // Records skipped during processing
    @Builder.Default // Lombok: default value used when building
    private List<String> errors = new ArrayList<>(); // Collected error messages
    private String message; // Human-friendly status message
}
 
