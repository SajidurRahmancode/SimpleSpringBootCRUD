package com.simple.crud.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;


@Profile("!prod") // Only apply OpenAPI in non-production
@OpenAPIDefinition( 
        // Sets the API metadata information
        info = @Info(
                // Title of the API
                title = "Simple Spring Boot CRUD API",
                version = "1.0.0",
                description = """
                        CRUD API for product management with user authentication,
                        role-based access control management features.
                        
                        ## Features include:
                        - User Authentication (JWT)
                        - Role-Based Access (USER, ADMIN)
                        - Product Management
                        - Image Upload Support
                        """
        ),
        servers = {
                // Development server configuration
                @Server(
                        // URL of the development server
                        url = "http://localhost:8082",
                        description = "Development Server"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        // Specifies the type of security scheme
        type = SecuritySchemeType.HTTP,
        // Specifies the HTTP authentication scheme
        scheme = "bearer",
        // Specifies the format of the bearer token
        bearerFormat = "JWT",
        // Description of the security scheme
        description = "JWT authentication token. Format: 'Bearer {token}'"
)
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}