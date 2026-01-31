package com.simple.crud.demo.security;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @Component marks this as a Spring bean to be managed by the container
@Component
// Extend OncePerRequestFilter to ensure this filter runs exactly once per request
// This filter intercepts every HTTP request to check for JWT tokens
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Final field to hold the JWT token provider (final = can't be changed after initialization)
    // This is used to validate JWT tokens and extract user information
    private final JwtTokenProvider tokenProvider;
    // Final field to hold the user details service
    // This is used to load user information from the database
    private final UserDetailsService userDetailsService;

    // @Autowired tells Spring to inject these dependencies when creating this filter
    // Constructor-based injection (recommended best practice for required dependencies)
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        // Assign the injected token provider to our field
        this.tokenProvider = tokenProvider;
        // Assign the injected user details service to our field
        this.userDetailsService = userDetailsService;
    }

    // @Override indicates we're implementing the abstract method from OncePerRequestFilter
    // This method is called for every HTTP request to check and process JWT authentication
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract the JWT token from the Authorization header in the request
        String jwt = resolveToken(request);
        // Check if a token exists (has text) AND if the token is valid (not expired, properly signed)
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            // Extract the user ID from the validated JWT token
            Long userId = tokenProvider.getUserIdFromToken(jwt);
            // Load the full user details from the database using the user ID
            UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
            // Create an authentication token with user details and their authorities (roles/permissions)
            // The second parameter (null) is for credentials - not needed since JWT already validated
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            // Set additional details about the request (IP address, session ID, etc.)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Store the authentication in the SecurityContext so Spring Security knows the user is authenticated
            // This makes the user available throughout the request processing
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continue with the next filter in the chain (pass request/response to next filter)
        // This is essential - without it, the request won't proceed to the controller
        filterChain.doFilter(request, response);
    }

    // Private helper method to extract the JWT token from the Authorization header
    // Returns the token string or null if not found
    private String resolveToken(HttpServletRequest request) {
        // Get the Authorization header value from the HTTP request
        // Typically looks like: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String bearerToken = request.getHeader("Authorization");
        // Check if the header has text AND starts with "Bearer " prefix (note the space)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract and return the actual token by removing the "Bearer " prefix
            // substring(7) removes the first 7 characters ("Bearer ")
            return bearerToken.substring(7);
        }
        // Return null if no valid Authorization header found
        return null;
    }
}
