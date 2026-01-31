// Package declaration - defines where this class belongs in the project structure
package com.simple.crud.demo.security;

// Import Date class for handling token expiration timestamps
import java.util.Date;
// Import StandardCharsets for UTF-8 encoding when converting strings to bytes
import java.nio.charset.StandardCharsets;
// Import Collectors utility for stream operations (combining authorities)
import java.util.stream.Collectors;

// Import @Value annotation to inject properties from application.properties file
import org.springframework.beans.factory.annotation.Value;
// Import Authentication interface that holds the authenticated user's information
import org.springframework.security.core.Authentication;
// Import GrantedAuthority interface that represents permissions/roles
import org.springframework.security.core.GrantedAuthority;
// Import @Component annotation to mark this as a Spring-managed bean
import org.springframework.stereotype.Component;

// Import Claims class that represents the JWT token's payload/body
import io.jsonwebtoken.Claims;
// Import Jwts class - the main entry point for JWT operations
import io.jsonwebtoken.Jwts;
// Import SignatureAlgorithm enum for specifying how to sign the JWT
import io.jsonwebtoken.SignatureAlgorithm;
// Import Decoders utility for decoding Base64-encoded strings
import io.jsonwebtoken.io.Decoders;
// Import Keys utility for creating cryptographic keys
import io.jsonwebtoken.security.Keys;

// Import SecretKey interface for representing cryptographic keys
import javax.crypto.SecretKey;

// @Component marks this class as a Spring bean so it can be autowired/injected
@Component
public class JwtTokenProvider {

    // @Value injects the JWT secret from application.properties, defaults to "change-this-secret" if not found
    // This secret is used to sign and verify JWT tokens
    @Value("${app.jwt.secret:change-this-secret}")
    private String jwtSecret;

    // @Value injects the token expiration time in milliseconds, defaults to 86400000ms (24 hours)
    // This determines how long a JWT token remains valid
    @Value("${app.jwt.expiration-ms:86400000}") // 24 hours
    private long jwtExpirationMs;

    // Private method to create a SecretKey object from the JWT secret string
    // This key is used for signing and verifying JWT tokens
    private SecretKey getSigningKey() {
        // Try to decode the secret assuming it's Base64-encoded
        try {
            // Decode the Base64-encoded secret string into raw bytes
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            // Create an HMAC-SHA key from the decoded bytes for signing JWTs
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            // If decoding fails (secret is not Base64), treat it as a plain string
            // fallback treat secret as raw string if not Base64
            // Convert the plain string secret to bytes using UTF-8 encoding
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            // Create an HMAC-SHA key from the plain string bytes
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    // Public method to generate a JWT token for an authenticated user
    // Takes the authentication object and user ID as parameters
    public String generateToken(Authentication authentication, Long userId) {
        // Extract all authorities (roles/permissions) from the authentication object
        // Stream through the authorities, get each authority name, and join them with commas
        String authorities = authentication.getAuthorities().stream()
                // Convert each GrantedAuthority to its string representation (e.g., "ROLE_USER")
                .map(GrantedAuthority::getAuthority)
                // Combine all authorities into a single comma-separated string
                .collect(Collectors.joining(","));

        // Create a Date object representing the current time (when token is issued)
        Date now = new Date();
        // Calculate the expiration date by adding the expiration duration to current time
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Build and return the JWT token as a string
        return Jwts.builder()
                // Set the subject (main identifier) of the token to the user's ID
                .setSubject(String.valueOf(userId))
                // Add a custom claim "roles" containing the user's authorities
                .claim("roles", authorities)
                // Set the issued-at timestamp to the current time
                .setIssuedAt(now)
                // Set when this token will expire
                .setExpiration(expiryDate)
                // Sign the token using our secret key and the HS256 algorithm
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // Compact/serialize the token into its final String form (header.payload.signature)
                .compact();
    }

    // Public method to validate whether a JWT token is valid (not expired, properly signed)
    // Returns true if valid, false otherwise
    public boolean validateToken(String token) {
        // Try to parse and validate the token
        try {
            // Build a JWT parser, configure it with our signing key, then parse the token
            // If parsing succeeds without exception, the token is valid
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            // Token is valid - return true
            return true;
        } catch (Exception ex) {
            // If any exception occurs (expired, malformed, wrong signature, etc.), token is invalid
            // Return false to indicate the token is not valid
            return false;
        }
    }

    // Public method to extract the user ID from a JWT token
    // Returns the user ID as a Long value
    public Long getUserIdFromToken(String token) {
        // Parse the token and extract the claims (payload data)
        // Build parser -> configure signing key -> parse token -> get the body/claims
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody();
        // Get the subject (which we stored as the user ID) and convert it to Long
        return Long.valueOf(claims.getSubject());
    }
}
