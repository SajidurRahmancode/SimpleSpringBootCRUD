package com.simple.crud.demo.config;
// Imports the OpenAPI class which is the main configuration object for OpenAPI/Swagger documentation
import io.swagger.v3.oas.models.OpenAPI;
// Imports the Tag class used to create logical groupings of API endpoints in documentation
import io.swagger.v3.oas.models.tags.Tag;
// Imports the Bean annotation to declare methods that return Spring-managed beans
import org.springframework.context.annotation.Bean;
// Imports the Configuration annotation to mark this class as a Spring configuration class
import org.springframework.context.annotation.Configuration;

// Imports the List utility for creating immutable lists
import java.util.List;

// Marks this class as a Spring configuration class that can provide bean definitions
@Configuration
// Declares the public configuration class for OpenAPI settings
public class OpenApiConfig {

    // Marks this method as a Spring bean producer that will be managed by the Spring container
    @Bean
    // Declares a public method that returns an OpenAPI configuration object
    public OpenAPI customOpenAPI() {
        // Returns a new OpenAPI configuration instance
        return new OpenAPI()
                // Sets the tags property with an immutable list of Tag objects
                .tags(List.of(
                        // Creates a new Tag for Authentication endpoints, setting name and descriptions
                        new Tag().name("Authentication").description("User and Admin authentication endpoints"),
                        // Creates a new Tag for Users endpoints, setting name and descriptions
                        new Tag().name("Users").description("User management operations (Admins only)"),
                        // Creates a new Tag for Products endpoints, setting name and descriptions
                        new Tag().name("Products").description("Product CRUD operations"),
                        // Creates a new Tag for Admin endpoints, setting name and descriptions
                        new Tag().name("Admin").description("Admin-specific operations")
                // Closes the List.of() method calls
                ));
    // Closes the customOpenAPI() method
    }
// Closes the OpenApiConfig class
}