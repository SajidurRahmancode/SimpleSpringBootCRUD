package com.simple.crud.demo.controller; // Controller package for REST endpoints

import com.simple.crud.demo.model.dto.BatchUploadResponseDto;
import com.simple.crud.demo.service.ProductBatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/products/batch") // Base path for all endpoints here
@RequiredArgsConstructor // Generates constructor for final fields
@Tag(name = "Product Batch", description = "Batch product upload endpoints") // Swagger tag grouping
public class ProductBatchController { // Handles batch product-related routes

        private final ProductBatchService productBatchService; // Service layer dependency

        @PostMapping("/upload") // Accepts CSV upload via multipart form
    @Operation(
                        summary = "Upload CSV for batch processing", // Brief summary for docs
                        description = "Uploads a CSV file and starts a batch job.", // Detailed description
                        security = @SecurityRequirement(name = "bearerAuth"), // Requires JWT auth
            responses = {
                    @ApiResponse(
                                                        responseCode = "200", // Success response code
                                                        description = "Upload accepted", // Success message
                                                        content = @Content(schema = @Schema(implementation = BatchUploadResponseDto.class)) // Response DTO
                    ),
                                        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content), // Bad request
                                        @ApiResponse(responseCode = "500", description = "Server error", content = @Content) // Server error
            }
    )
        public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) { // Endpoint method signature
                try { // Attempt processing
                        BatchUploadResponseDto response = productBatchService.uploadAndProcessCsv(file); // Delegate to service
                        return ResponseEntity.ok(response); // Return 200 with DTO
                } catch (IllegalArgumentException ex) { // Handle validation errors
                        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())); // Return 400 with message
                } catch (Exception ex) { // Handle unexpected errors
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error")); // Return 500
                }
        }

        @GetMapping("/status/{jobExecutionId}") // Query job status by ID
    @Operation(
                        summary = "Get batch job status", // Summary
                        parameters = @Parameter(name = "jobExecutionId", in = ParameterIn.PATH, required = true, description = "Job execution ID"), // Path variable doc
            responses = {
                    @ApiResponse(
                                                        responseCode = "200", // Success
                                                        description = "Status retrieved", // Description
                                                        content = @Content(schema = @Schema(implementation = BatchUploadResponseDto.class)) // DTO content
                    ),
                                        @ApiResponse(responseCode = "404", description = "Job not found", content = @Content) // Not found
            }
    )
        public ResponseEntity<BatchUploadResponseDto> getJobStatus(@PathVariable Long jobExecutionId) { // Status endpoint
                BatchUploadResponseDto response = productBatchService.getJobStatus(jobExecutionId); // Delegate to service
                if (response == null) { // If job not found
                        return ResponseEntity.notFound().build(); // Return 404
                }
                return ResponseEntity.ok(response); // Return 200 with DTO
        }

        @GetMapping("/template") // Download a sample CSV template
    @Operation(
                        summary = "Download CSV template", // Summary
                        description = "Download a sample CSV template file with the correct format for batch product upload", // Description
                        responses = @ApiResponse(responseCode = "200", description = "Template downloaded successfully") // Success doc
    )
        public ResponseEntity<Resource> downloadTemplate() { // Template endpoint handler
                String csvContent = """
                                name,description,price,stockQuantity
                                Sample Laptop,High-performance laptop for professionals,1299.99,25
                                USB-C Cable,Premium USB-C charging cable 2m,19.99,200
                                Mechanical Keyboard,RGB mechanical gaming keyboard,89.99,50
                                """; // Multi-line CSV content
                ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes()); // Wrap content as a resource
                return ResponseEntity.ok() // Return 200 OK
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_template.csv") // Force download name
                                .contentType(MediaType.parseMediaType("text/csv")) // Content type as CSV
                                .body(resource); // Body with resource
        }
}
