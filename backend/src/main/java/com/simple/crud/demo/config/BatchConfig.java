package com.simple.crud.demo.config; // Package grouping for configuration classes

import com.simple.crud.demo.model.dto.ProductCsvRecord;
import com.simple.crud.demo.model.entity.Product;
import com.simple.crud.demo.repository.ProductRepository;
import com.simple.crud.demo.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration // Marks this class as a Spring configuration source
@RequiredArgsConstructor // Generates constructor for all final fields
@Slf4j // Enables logging via 'log'
public class BatchConfig { // Spring Batch configuration for CSV import

    private final Validator validator; // Bean validator for DTO validation
    private final ProductRepository productRepository; // Repository for persisting products
    private final UserRepository userRepository; // Repository to resolve users (future use)
    private final JobRepository jobRepository; // Spring Batch job metadata repository
    private final PlatformTransactionManager transactionManager; // Transaction manager for chunking

    @Bean // Registers the reader bean in the Spring context
    @StepScope // Reader is created per step execution; allows jobParameters injection
    public FlatFileItemReader<ProductCsvRecord> csvReader(@Value("#{jobParameters['filePath']}") String filePath) { // Reads CSV rows into ProductCsvRecord
        FlatFileItemReader<ProductCsvRecord> reader = new FlatFileItemReader<>(); // Instantiate a flat file reader
        reader.setResource(new FileSystemResource(filePath)); // Source file provided via job parameter
        reader.setLinesToSkip(1); // Skip header line

        DefaultLineMapper<ProductCsvRecord> lineMapper = new DefaultLineMapper<>(); // Maps each line to a DTO
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(); // Splits lines by delimiter
        tokenizer.setDelimiter(","); // Use comma as delimiter
        tokenizer.setNames("name", "description", "price", "stockQuantity"); // Expected column names

        BeanWrapperFieldSetMapper<ProductCsvRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>(); // Map fields to DTO
        fieldSetMapper.setTargetType(ProductCsvRecord.class); // Target DTO type

        lineMapper.setLineTokenizer(tokenizer); // Attach tokenizer
        lineMapper.setFieldSetMapper(fieldSetMapper); // Attach mapper
        reader.setLineMapper(lineMapper); // Configure reader mapping
        return reader; // Return configured reader
    }

    @Bean // Registers the processor bean
    public ItemProcessor<ProductCsvRecord, Product> productProcessor() { // Validates and transforms DTO to entity
        return csvRecord -> { // Item-by-item processing
            Set<ConstraintViolation<ProductCsvRecord>> violations = validator.validate(csvRecord); // Validate fields
            if (!violations.isEmpty()) { // If validation fails
                throw new IllegalArgumentException("Validation failed for record: " + csvRecord); // Fail the item
            }

            Product product = Product.builder() // Build Product entity
                    .name(csvRecord.getName()) // Copy name
                    .description(csvRecord.getDescription()) // Copy description
                    .price(csvRecord.getPrice()) // Copy price
                    .stockQuantity(csvRecord.getStockQuantity() != null ? csvRecord.getStockQuantity() : 0) // Default stock
                    .updatedAt(LocalDateTime.now()) // Timestamp update
                    .build(); // Finish building entity
            return product; // Emit entity to writer
        }; // End lambda
    }

    @Bean // Registers the writer bean
    public ItemWriter<Product> productWriter() { // Persists products in chunks
        return items -> { // Write callback for batch chunk
            productRepository.saveAll(items); // Bulk save via JPA repository
            log.info("Saved {} products", items.size()); // Log number of persisted records
        }; // End writer
    }

    @Bean // Registers the step used by the job
    public Step importProductStep(ItemReader<ProductCsvRecord> productCsvReader, // Inject reader
                                  ItemProcessor<ProductCsvRecord, Product> productProcessor, // Inject processor
                                  ItemWriter<Product> productWriter) { // Inject writer
        return new StepBuilder("importProductStep", jobRepository) // Create step builder with job repository
                .<ProductCsvRecord, Product>chunk(100, transactionManager) // Define chunk size and transaction manager
                .reader(productCsvReader) // Use our reader
                .processor(productProcessor) // Use our processor
                .writer(productWriter) // Use our writer
                .faultTolerant() // Enable fault tolerance
                .skipLimit(1000) // Allow up to 1000 skipped items
                .skip(Exception.class) // Skip on any exception
                .build(); // Build the step
    }

    @Bean // Registers the job that runs the step
    public Job importProductJob(Step importProductStep) { // Single-step job for product import
        return new JobBuilder("importProductJob", jobRepository) // Create job builder
                .start(importProductStep) // Start with the import step
                .build(); // Build the job
    }
}
