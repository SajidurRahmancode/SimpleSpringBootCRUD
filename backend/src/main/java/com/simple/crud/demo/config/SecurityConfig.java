// Package declaration - this is a configuration class for the application
package com.simple.crud.demo.config;

// Import @Bean annotation to define Spring beans
import org.springframework.context.annotation.Bean;
// Import @Configuration to mark this as a configuration class
import org.springframework.context.annotation.Configuration;
// Import HttpMethod enum for specifying HTTP methods (GET, POST, etc.)
import org.springframework.http.HttpMethod;
// Import AuthenticationManager interface for handling authentication
import org.springframework.security.authentication.AuthenticationManager;
// Import AuthenticationConfiguration to get the authentication manager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Import @EnableMethodSecurity to enable method-level security annotations like @PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// Import HttpSecurity for configuring web-based security
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Import @EnableWebSecurity to enable Spring Security's web security support
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import SessionCreationPolicy enum for controlling session creation
import org.springframework.security.config.http.SessionCreationPolicy;
// Import BCryptPasswordEncoder for password hashing using BCrypt algorithm
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Import PasswordEncoder interface for password encoding
import org.springframework.security.crypto.password.PasswordEncoder;
// Import SecurityFilterChain to define the security filter chain
import org.springframework.security.web.SecurityFilterChain;
// Import UsernamePasswordAuthenticationFilter to specify where to add our JWT filter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Import CorsConfiguration for configuring CORS policies
import org.springframework.web.cors.CorsConfiguration;
// Import CorsConfigurationSource interface for providing CORS configuration
import org.springframework.web.cors.CorsConfigurationSource;
// Import UrlBasedCorsConfigurationSource to register CORS configuration by URL pattern
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Import Arrays utility for creating arrays
import java.util.Arrays;
// Import List interface for creating lists
import java.util.List;

// Import our custom JWT authentication filter
import com.simple.crud.demo.security.JwtAuthenticationFilter;

// @Configuration marks this as a Spring configuration class
@Configuration
// @EnableWebSecurity enables Spring Security's web security features
@EnableWebSecurity
// @EnableMethodSecurity enables @PreAuthorize, @PostAuthorize annotations on methods
@EnableMethodSecurity
public class SecurityConfig {

    // @Bean tells Spring to create and manage this object as a bean
    // This bean can be injected wherever password encoding is needed
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Return a BCryptPasswordEncoder instance for secure password hashing
        // BCrypt is a strong, adaptive hashing algorithm resistant to brute-force attacks
        return new BCryptPasswordEncoder();
    }

    // @Bean defines the main security filter chain that processes every HTTP request
    // Spring automatically injects HttpSecurity and our custom JwtAuthenticationFilter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        // Configure the HttpSecurity object using method chaining
        http
            // Enable CORS (Cross-Origin Resource Sharing) using our custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF (Cross-Site Request Forgery) protection since we're using JWT (stateless)
            // CSRF protection is not needed for stateless APIs that don't use session cookies
            .csrf(csrf -> csrf.disable())
            // Configure session management to be STATELESS (no server-side sessions)
            // Each request is authenticated independently via JWT token
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configure authorization rules for different URL patterns
            .authorizeHttpRequests(authz -> authz
                // Public endpoints that anyone can access without authentication
                // Allow all authentication endpoints (login, register) to be public
                .requestMatchers("/api/auth/**").permitAll()
                // Allow versioned auth endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Allow admin authentication endpoint
                .requestMatchers("/api/admin/auth/**").permitAll()
                // Allow GET requests to uploaded files (images, etc.)
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // Allow anyone to view products (GET requests only)
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/v1/products/**").permitAll()
                // Swagger/OpenAPI endpoints - allow public access to API documentation
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Admin-only endpoints - only users with ADMIN role can access
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin role required for /api/admin/**
                // User management endpoints - admin only
                .requestMatchers("/api/users/**").hasRole("ADMIN") // Admin role required for /api/users/**
                // All other endpoints require authentication (any authenticated user)
                .anyRequest().authenticated()
            )
            // Configure HTTP headers - disable frame options for H2 console or other embedded content
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            // Add our JWT authentication filter before the standard username/password filter
            // This ensures JWT tokens are checked before traditional authentication
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the configured SecurityFilterChain
        return http.build();
    }

    // @Bean creates an AuthenticationManager bean that handles authentication logic
    // Spring automatically injects the AuthenticationConfiguration
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Get the default AuthenticationManager from Spring's configuration
        // This manager coordinates the authentication process using our UserDetailsService
        return configuration.getAuthenticationManager();
    }

    // @Bean creates a CORS configuration source to handle cross-origin requests
    // This is necessary when frontend and backend run on different ports/domains
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Create a new CORS configuration object
        CorsConfiguration configuration = new CorsConfiguration();
        // Set which origins (domains) are allowed to make requests to our API
        // Here we allow localhost on ports 3000 and 3001 (typical React dev servers)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        // Set which HTTP methods are allowed (GET, POST, etc.)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Allow all headers in requests ("*" means any header)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Expose the Authorization header in responses so frontend can read JWT tokens
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        // Allow credentials (cookies, authorization headers) to be included in requests
        configuration.setAllowCredentials(true);

        // Create a URL-based CORS configuration source
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Register our CORS configuration for all paths (/**)
        source.registerCorsConfiguration("/**", configuration);
        // Return the configured source
        return source;
    }
}
