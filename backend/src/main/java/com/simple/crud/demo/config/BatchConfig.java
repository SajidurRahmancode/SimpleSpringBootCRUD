package com.simple.crud.demo.config; // Package grouping for configuration classes

import com.simple.crud.demo.model.dto.ProductCsvRecordDto;
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


// for importing products from uploaded CSV files
public class BatchConfig { // Spring Batch configuration for CSV import
    // Dependencies injected via constructor
    private final Validator validator; // Bean validator for DTO validation
    private final ProductRepository productRepository; // Repository for persisting products
    private final UserRepository userRepository; // Repository to resolve users (future use)
    private final JobRepository jobRepository; // Spring Batch job metadata repository
    private final PlatformTransactionManager transactionManager; // Transaction manager for chunking

    @Bean // Registers the reader bean in the Spring context and can inject elsewhere
    @StepScope // Reader is created per step execution;NEW instance for each step execution, allows jobParameters injection, different file for each job is allowed now 
    // Reader to parse CSV into ProductCsvRecordDto
    // Uses job parameter 'filePath' to locate the uploaded CSV file 
    // Configured to skip header and map fields appropriately
    // Reads each line, tokenizes by comma, and maps to DTO
    // with validation handled in processor 
    // to ensure data integrity before processing
    public FlatFileItemReader<ProductCsvRecordDto> csvReader(@Value("#{jobParameters['filePath']}") String filePath) { //Spring Expression Language (SpEL) Reads the CSV rows into ProductCsvRecordDto
        /* 
        reading flow 
        1. Reader reads line: "Laptop,Gaming laptop,1899.99,15"
            String line = readLine();


        2. Passes to LineMapper
            ProductCsvRecord record = lineMapper.mapLine(line, 0);

        3. LineMapper uses Tokenizer:
            String[] tokens = tokenizer.tokenize(line);
            tokens = ["Laptop", "Gaming laptop", "1899.99", "15"]

        4. LineMapper uses FieldMapper:
            ProductCsvRecord record = new ProductCsvRecord();
            fieldSetMapper.mapFieldSet(tokens);
            Calls: setName("Laptop"), setDescription("Gaming laptop"), etc.

        5. Returns the populated object
            return record;
        */
        FlatFileItemReader<ProductCsvRecordDto> reader = new FlatFileItemReader<>(); // Instantiate a flat file reader
        reader.setResource(new FileSystemResource(filePath)); // Source file provided via job parameter, tell reader which file to read
        reader.setLinesToSkip(1); // Skip header line

        DefaultLineMapper<ProductCsvRecordDto> lineMapper = new DefaultLineMapper<>(); // Maps each line to a DTO ,line mapper Converts a line of text → Java object
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(); // Splits lines by delimiter, Tokenizer:  Split line into fields
        tokenizer.setDelimiter(","); // Use comma as delimiter
        tokenizer.setNames("name", "description", "price", "stockQuantity"); // Expected column names

        BeanWrapperFieldSetMapper<ProductCsvRecordDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>(); // Map fields to DTO,Maps tokenized fields to Java object Spring's utility for accessing object properties
        fieldSetMapper.setTargetType(ProductCsvRecordDto.class); // Target DTO type,Tells mapper which class to create

        lineMapper.setLineTokenizer(tokenizer); // Attach tokenizer
        //Raw CSV Line → [Tokenizer] → Array of Strings → [FieldMapper] → Java Object
        lineMapper.setFieldSetMapper(fieldSetMapper); // Attach mapper ,After tokenizing, use this mapper to create objects 
        
        reader.setLineMapper(lineMapper); // Configure reader mapping
        return reader; // Return configured reader
    }

    @Bean // Registers the processor bean
    // Processor to validate and convert DTO to entity
    // Validates each ProductCsvRecordDto using Bean Validation
    // If validation fails, throws exception to skip the item
    // On success, maps fields to Product entity
    // setting default values as needed
    public ItemProcessor<ProductCsvRecordDto, Product> productProcessor() { // Validates and transforms DTO to entity
        return csvRecord -> { //lambda , Function without a name, Item-by-item processing
            Set<ConstraintViolation<ProductCsvRecordDto>> violations = validator.validate(csvRecord); // Validate fields,Checks all validation annotations on ProductCsvRecordDto
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
    // Writer to persist Product entities in chunks
    // Uses ProductRepository to bulk save items
    // Logs the number of items saved for monitoring
    // Handles lists of Product entities emitted by the processor
    // and persists them to the database
    // with efficient batch operations
    // leveraging Spring Data JPA's saveAll method
    public ItemWriter<Product> productWriter() { // Persists products in chunks
        return items -> { // lambda function, Writes callback for batch chunk
            productRepository.saveAll(items); // Bulk save via JPA repository
            log.info("Saved {} products", items.size()); // Log number of persisted records
        }; // End writer
    }

    @Bean // Registers the step used by the job
    // Step defining the chunk-oriented processing
    // Configures reader, processor, and writer
    // Sets chunk size and fault tolerance
    // to handle large files efficiently
    // with skipping of faulty records
    // encapsulates the entire read-process-write cycle
    // with error handling and transaction management
    public Step importProductStep(ItemReader<ProductCsvRecordDto> productCsvReader, // Inject reader
                                  ItemProcessor<ProductCsvRecordDto, Product> productProcessor, // Inject processor
                                  ItemWriter<Product> productWriter) { // Inject writer
        return new StepBuilder("importProductStep", jobRepository) // Create step builder with job repository
                .<ProductCsvRecordDto, Product>chunk(100, transactionManager) // Define chunk size and transaction manager
                .reader(productCsvReader) // Use our reader
                .processor(productProcessor) // Use our processor
                .writer(productWriter) // Use our writer
                .faultTolerant() // Enable fault tolerance
                .skipLimit(1000) // Allow up to 1000 skipped items
                .skip(Exception.class) // Skip on any exception
                .build(); // Build the step
    }

    @Bean // Registers the job that runs the step
    // Job definition for importing products
    // Single step job that executes the importProductStep
    // Configured with the job repository
    // encapsulates the entire batch process
    // from reading the CSV to persisting products
    public Job importProductJob(Step importProductStep) { // Single-step job for product import
        return new JobBuilder("importProductJob", jobRepository) // Create job builder
                .start(importProductStep) // Start with the import step
                .build(); // Build the job
    }
}
