# Swagger/OpenAPI Documentation - Simple Spring Boot CRUD API

## Table of Contents
1. [Overview](#overview)
2. [Configuration](#configuration)
3. [Security Setup](#security-setup)
4. [Swagger Annotations Reference](#swagger-annotations-reference)
5. [API Endpoints Documentation](#api-endpoints-documentation)
6. [Best Practices](#best-practices)
7. [Testing the API](#testing-the-api)

---

## Overview

This project uses **Springdoc OpenAPI 3.0** (Swagger) for comprehensive API documentation and interactive testing.

### Technology Stack
- **Spring Boot**: 3.x
- **Springdoc OpenAPI**: 2.3.0
- **OpenAPI Specification**: 3.0
- **Authentication**: JWT Bearer Token

### Access Points
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

---

## Configuration

### 1. Maven Dependency

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2. Application Configuration

**File**: `application.properties`

```properties
# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true
```

### 3. OpenAPI Configuration

**File**: `DemoApplication.java`

```java
@OpenAPIDefinition(
        info = @Info(
                title = "Simple Spring Boot CRUD API",
                version = "1.0.0",
                description = """
                        A comprehensive CRUD API for product management with user authentication,
                        role-based access control, and supplier management features.
                        
                        ## Features
                        - User Authentication (JWT)
                        - Role-Based Access (USER, ADMIN, SUPPLIER)
                        - Product Management
                        - Supplier Applications
                        - Image Upload Support
                        """,
                contact = @Contact(
                        name = "API Support",
                        email = "support@example.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8082",
                        description = "Development Server"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication token. Format: 'Bearer {token}'"
)
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## Security Setup

### Spring Security Configuration

**File**: `SecurityConfig.java`

```java
.authorizeHttpRequests(authz -> authz
    // Swagger endpoints are publicly accessible
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    // Other endpoints...
)
```

### JWT Authentication in Swagger

1. Click **"Authorize"** button (🔒 icon) in Swagger UI
2. Enter: `Bearer your-jwt-token-here`
3. Click **"Authorize"**
4. Test protected endpoints

---

## Swagger Annotations Reference

### 1. `@Tag`
**Purpose**: Groups related endpoints together in Swagger UI

**Location**: Controller class level

**Example**:
```java
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    // ...
}
```

**Attributes**:
- `name`: Tag name (displayed in Swagger UI)
- `description`: Tag description

---

### 2. `@Operation`
**Purpose**: Documents individual API endpoints

**Location**: Method level (above request mapping)

**Example**:
```java
@Operation(
        summary = "Get all products",
        description = "Retrieve paginated list of all products. Supports sorting.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
        }
)
@GetMapping
public ResponseEntity<Page<ProductResponseDto>> getAllProducts(...) {
    // ...
}
```

**Attributes**:
- `summary`: Short description (1 line)
- `description`: Detailed description (can be multi-line)
- `responses`: Array of possible responses
- `security`: Security requirements for this endpoint
- `requestBody`: Request body documentation

---

### 3. `@Parameter`
**Purpose**: Documents path variables, query parameters, and headers

**Location**: Method parameter level

**Example**:
```java
@GetMapping("/{id}")
public ResponseEntity<ProductResponseDto> getProductById(
        @Parameter(
                name = "id",
                description = "Product ID",
                required = true,
                example = "1",
                in = ParameterIn.PATH
        )
        @PathVariable Long id) {
    // ...
}
```

**Attributes**:
- `name`: Parameter name
- `description`: What the parameter is for
- `required`: Whether the parameter is mandatory
- `example`: Sample value for testing
- `in`: Parameter location (`PATH`, `QUERY`, `HEADER`, `COOKIE`)

---

### 4. `@ApiResponse`
**Purpose**: Documents possible HTTP response codes

**Location**: Inside `@Operation` responses array

**Example**:
```java
responses = {
        @ApiResponse(
                responseCode = "200",
                description = "Product found",
                content = @Content(schema = @Schema(implementation = ProductResponseDto.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Product not found"
        )
}
```

**Attributes**:
- `responseCode`: HTTP status code ("200", "404", etc.)
- `description`: What this response means
- `content`: Response body structure

---

### 5. `@RequestBody` (Swagger)
**Purpose**: Documents request body structure for POST/PUT endpoints

**Location**: Inside `@Operation` annotation

**Full Qualified Name**: `@io.swagger.v3.oas.annotations.parameters.RequestBody`

**Example**:
```java
@Operation(
        summary = "Create product",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product creation details",
                required = true,
                content = @Content(schema = @Schema(implementation = ProductCreateDto.class))
        )
)
@PostMapping
public ResponseEntity<?> createProduct(
        @Valid @RequestBody ProductCreateDto dto) {
    // ...
}
```

**Attributes**:
- `description`: What the request body contains
- `required`: Whether body is mandatory
- `content`: Request body schema

**⚠️ Important**: This is different from Spring's `@RequestBody`:
- **Swagger's `@RequestBody`**: Documentation only (in `@Operation`)
- **Spring's `@RequestBody`**: Runtime data binding (on method parameter)

---

### 6. `@SecurityRequirement`
**Purpose**: Marks endpoints that require authentication

**Location**: Inside `@Operation` annotation

**Example**:
```java
@Operation(
        summary = "Get my products",
        security = @SecurityRequirement(name = "bearerAuth"),
        // ...
)
@GetMapping("/my")
public ResponseEntity<Page<ProductResponseDto>> getMyProducts(...) {
    // ...
}
```

**Attributes**:
- `name`: Security scheme name (defined in `@SecurityScheme` at app level)

---

### 7. `@Schema`
**Purpose**: Documents DTO/model structure in Swagger

**Location**: DTO class level or field level

**Example**:
```java
@Schema(description = "Product data transfer object")
public class ProductCreateDto {
    
    @Schema(description = "Product name", example = "Laptop", required = true)
    @NotBlank
    private String name;
    
    @Schema(description = "Product price", example = "999.99", required = true)
    @NotNull
    private BigDecimal price;
}
```

**Attributes**:
- `description`: Field description
- `example`: Sample value
- `required`: Whether field is mandatory
- `implementation`: Class type for complex objects

---

### 8. `@Content`
**Purpose**: Specifies content type and schema for request/response

**Location**: Inside `@ApiResponse` or `@RequestBody`

**Example**:
```java
content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ProductResponseDto.class)
)
```

**Attributes**:
- `mediaType`: Content type (default: "application/json")
- `schema`: Data structure

---

### 9. `ParameterIn` (Enum)
**Purpose**: Specifies where the parameter is located

**Location**: Inside `@Parameter` annotation

**Values**:
- `ParameterIn.PATH`: URL path parameter (`/products/{id}`)
- `ParameterIn.QUERY`: Query string parameter (`?page=0&size=10`)
- `ParameterIn.HEADER`: HTTP header
- `ParameterIn.COOKIE`: Cookie value

**Example**:
```java
@Parameter(
        name = "page",
        in = ParameterIn.QUERY
)
@RequestParam(value = "page", defaultValue = "0") int page
```

---

## API Endpoints Documentation

### Authentication Endpoints

#### 1. User Registration
- **Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**: `UserCreateDto`
- **Response**: `AuthResponseDto` with JWT token
- **Status Codes**:
  - `201`: User created successfully
  - `400`: Invalid input or user already exists

**Implementation**:
```java
@PostMapping("/register")
@Operation(
        summary = "Register new user",
        description = "Create a new user account with USER role",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                required = true,
                content = @Content(schema = @Schema(implementation = UserCreateDto.class))
        ),
        responses = {
                @ApiResponse(responseCode = "201", description = "User created successfully",
                        content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
        }
)
public ResponseEntity<?> register(@Valid @RequestBody UserCreateDto dto) {
    // Implementation
}
```

---

#### 2. User Login
- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**: `LoginRequestDto`
- **Response**: `AuthResponseDto` with JWT token
- **Status Codes**:
  - `200`: Login successful
  - `401`: Invalid credentials

---

#### 3. Get Current User
- **Endpoint**: `GET /api/auth/me`
- **Access**: Authenticated users only
- **Security**: Bearer token required
- **Response**: `UserResponseDto`
- **Status Codes**:
  - `200`: User found
  - `401`: Not authenticated

**Implementation**:
```java
@GetMapping("/me")
@Operation(
        summary = "Get current user",
        description = "Get authenticated user's profile information",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
                @ApiResponse(responseCode = "200", description = "User found"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
        }
)
public ResponseEntity<?> me(Authentication authentication) {
    // Implementation
}
```

---

### Admin Authentication Endpoints

#### 4. Admin Registration
- **Endpoint**: `POST /api/admin/auth/register`
- **Access**: Public (requires admin secret key)
- **Request Body**: `AdminRegisterDto`
- **Response**: `AuthResponseDto` with JWT token
- **Status Codes**:
  - `201`: Admin account created
  - `400`: Invalid input or wrong admin secret

---

#### 5. Admin Login
- **Endpoint**: `POST /api/admin/auth/login`
- **Access**: Public
- **Request Body**: `Map<String, String>` (username, password)
- **Response**: `AuthResponseDto` with JWT token
- **Status Codes**:
  - `200`: Login successful
  - `401`: Invalid credentials or not an admin account
  - `400`: Username and password are required

---

#### 6. Get Admin Profile
- **Endpoint**: `GET /api/admin/auth/profile/{username}`
- **Access**: Public
- **Path Parameter**: `username` (Admin username)
- **Response**: `UserResponseDto`
- **Status Codes**:
  - `200`: Admin profile found
  - `404`: Admin not found

**Implementation**:
```java
@GetMapping("/profile/{username}")
@Operation(
        summary = "Get admin profile",
        description = "Retrieve admin profile information by username",
        responses = {
                @ApiResponse(responseCode = "200", description = "Admin profile found",
                        content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                @ApiResponse(responseCode = "404", description = "Admin not found")
        }
)
public ResponseEntity<?> getAdminProfile(
        @Parameter(
                name = "username",
                description = "Admin username",
                required = true,
                example = "admin",
                in = ParameterIn.PATH
        )
        @PathVariable String username) {
    // Implementation
}
```

---

### Product Endpoints

#### 7. Get All Products
- **Endpoint**: `GET /api/products`
- **Access**: Public
- **Query Parameters**:
  - `page` (default: 0) - Page number (0-based)
  - `size` (default: 10) - Page size
  - `sort` (default: "id,asc") - Sort criteria (field,direction)
- **Response**: `Page<ProductResponseDto>`
- **Status Codes**:
  - `200`: Products retrieved successfully

**Implementation**:
```java
@GetMapping
@Operation(
        summary = "Get all products",
        description = "Retrieve paginated list of all products. Supports sorting.",
        responses = {
                @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
        }
)
public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
        @Parameter(
                name = "page",
                description = "Page number (0-based)",
                example = "0",
                in = ParameterIn.QUERY
        )
        @RequestParam(value = "page", defaultValue = "0") int page,
        
        @Parameter(
                name = "size",
                description = "Page size",
                example = "10",
                in = ParameterIn.QUERY
        )
        @RequestParam(value = "size", defaultValue = "10") int size,
        
        @Parameter(
                name = "sort",
                description = "Sort criteria (field,direction)",
                example = "name,asc",
                in = ParameterIn.QUERY
        )
        @RequestParam(value = "sort", defaultValue = "id,asc") String sort) {
    // Implementation
}
```

---

#### 8. Get My Products
- **Endpoint**: `GET /api/products/my`
- **Access**: Authenticated users only
- **Security**: Bearer token required
- **Query Parameters**:
  - `page` (default: 0)
  - `size` (default: 10)
- **Response**: `Page<ProductResponseDto>`
- **Status Codes**:
  - `200`: Products retrieved successfully
  - `401`: Not authenticated

---

#### 9. Get Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Access**: Public
- **Path Parameter**: `id` (Product ID)
- **Response**: `ProductResponseDto`
- **Status Codes**:
  - `200`: Product found
  - `404`: Product not found

**Implementation**:
```java
@GetMapping("/{id}")
@Operation(
        summary = "Get product by ID",
        description = "Retrieve a single product by its ID",
        responses = {
                @ApiResponse(responseCode = "200", description = "Product found",
                        content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                @ApiResponse(responseCode = "404", description = "Product not found")
        }
)
public ResponseEntity<ProductResponseDto> getProductById(
        @Parameter(
                name = "id",
                description = "Product ID",
                required = true,
                example = "1",
                in = ParameterIn.PATH
        )
        @PathVariable Long id) {
    // Implementation
}
```

---

#### 10. Search Products
- **Endpoint**: `GET /api/products/search`
- **Access**: Public
- **Query Parameters**:
  - `q` (required) - Search query
  - `page` (default: 0)
  - `size` (default: 10)
- **Response**: `Page<ProductResponseDto>`
- **Status Codes**:
  - `200`: Products found

---

#### 11. Create Product (JSON)
- **Endpoint**: `POST /api/products`
- **Access**: Authenticated users only
- **Security**: Bearer token required
- **Request Body**: `ProductCreateDto`
- **Response**: `ProductResponseDto`
- **Status Codes**:
  - `201`: Product created
  - `400`: Invalid input
  - `401`: Not authenticated

**Implementation**:
```java
@PostMapping
@Operation(
        summary = "Create product (JSON)",
        description = "Create a new product without image",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product creation details",
                required = true,
                content = @Content(schema = @Schema(implementation = ProductCreateDto.class))
        ),
        responses = {
                @ApiResponse(responseCode = "201", description = "Product created",
                        content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
        }
)
public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto dto) {
    // Implementation
}
```

---

#### 12. Create Product with Image
- **Endpoint**: `POST /api/products` (multipart/form-data)
- **Access**: Authenticated users only
- **Security**: Bearer token required
- **Request**: Multipart form data
  - Product data: `ProductCreateDto`
  - Image file: Optional, max 5MB
- **Response**: `ProductResponseDto`
- **Status Codes**:
  - `201`: Product created with image
  - `400`: Invalid input or file too large
  - `401`: Not authenticated

**Implementation**:
```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(
        summary = "Create product with image",
        description = "Create a new product with optional image upload (max 5MB, jpg/png/gif)",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
                @ApiResponse(responseCode = "201", description = "Product created with image",
                        content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input or file too large"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
        }
)
public ResponseEntity<?> createProductMultipart(
        @ModelAttribute @Valid ProductCreateDto dto,
        @Parameter(
                name = "image",
                description = "Product image file (optional, max 5MB)",
                required = false,
                content = @Content(mediaType = "multipart/form-data")
        )
        @RequestPart(value = "image", required = false) MultipartFile image) {
    // Implementation
}
```

---

#### 13. Update Product
- **Endpoint**: `PUT /api/products/{id}`
- **Access**: Authenticated users (owner or admin)
- **Security**: Bearer token required
- **Path Parameter**: `id` (Product ID)
- **Request Body**: `ProductCreateDto`
- **Response**: `ProductResponseDto`
- **Status Codes**:
  - `200`: Product updated
  - `403`: Access denied (not owner or admin)
  - `404`: Product not found
  - `401`: Not authenticated

---

#### 14. Delete Product
- **Endpoint**: `DELETE /api/products/{id}`
- **Access**: Authenticated users (owner or admin)
- **Security**: Bearer token required
- **Path Parameter**: `id` (Product ID)
- **Response**: Success message
- **Status Codes**:
  - `200`: Product deleted
  - `403`: Access denied (not owner or admin)
  - `404`: Product not found
  - `401`: Not authenticated

**Implementation**:
```java
@DeleteMapping("/{id}")
@Operation(
        summary = "Delete product",
        description = "Delete a product. Only owner or admin can delete.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
                @ApiResponse(responseCode = "200", description = "Product deleted"),
                @ApiResponse(responseCode = "403", description = "Access denied"),
                @ApiResponse(responseCode = "404", description = "Product not found")
        }
)
public ResponseEntity<?> deleteProduct(
        @Parameter(
                name = "id",
                description = "Product ID to delete",
                required = true,
                example = "1",
                in = ParameterIn.PATH
        )
        @PathVariable Long id) {
    // Implementation
}
```

---

### User Management Endpoints (Admin Only)

#### 15. Get All Users
- **Endpoint**: `GET /api/users`
- **Access**: Admin only
- **Security**: Bearer token required (ADMIN role)
- **Query Parameters**:
  - `page` (default: 0)
  - `size` (default: 10)
- **Response**: `Page<UserResponseDto>`
- **Status Codes**:
  - `200`: Users retrieved successfully
  - `403`: Access denied (not admin)

---

#### 16. Get User by ID
- **Endpoint**: `GET /api/users/{id}`
- **Access**: Admin only
- **Security**: Bearer token required (ADMIN role)
- **Path Parameter**: `id` (User ID)
- **Response**: `UserResponseDto`
- **Status Codes**:
  - `200`: User found
  - `404`: User not found
  - `403`: Access denied

---

#### 17. Delete User
- **Endpoint**: `DELETE /api/users/{id}`
- **Access**: Admin only
- **Security**: Bearer token required (ADMIN role)
- **Path Parameter**: `id` (User ID)
- **Response**: Success message
- **Status Codes**:
  - `200`: User deleted
  - `404`: User not found
  - `403`: Access denied

---

## Best Practices

### 1. Annotation Placement

✅ **Correct**:
```java
@Tag(name = "Products", description = "...")  // Controller level
public class ProductController {
    
    @Operation(summary = "...", description = "...")  // Method level
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(name = "id", ...)  // Parameter level
            @PathVariable Long id) {
        // ...
    }
}
```

---

### 2. RequestBody Annotation Conflict

**Problem**: Both Spring and Swagger have `@RequestBody` annotations

**Solution**: Use Spring's `@RequestBody` for method parameters, Swagger's fully qualified in `@Operation`

✅ **Correct**:
```java
@Operation(
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(...)  // Swagger
)
public ResponseEntity<?> create(@Valid @RequestBody ProductDto dto) {  // Spring
    // ...
}
```

❌ **Incorrect**:
```java
// Don't use Spring's RequestBody in @Operation
@Operation(
        requestBody = @RequestBody(...)  // Wrong! This is Spring's annotation
)
```

---

### 3. Example Values

Always provide realistic examples:
```java
@Parameter(
        name = "id",
        example = "1",  // ✅ Realistic
        // NOT example = "123456789"  // ❌ Unrealistic
)
```

---

### 4. Response Documentation

Document ALL possible response codes:
```java
responses = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "500", description = "Server Error")
}
```

---

### 5. Security Documentation

Mark ALL protected endpoints:
```java
@Operation(
        summary = "...",
        security = @SecurityRequirement(name = "bearerAuth")  // ✅ Always add this
)
```

---

### 6. ParameterIn Usage

Always specify parameter location:
```java
@Parameter(
        name = "id",
        in = ParameterIn.PATH  // ✅ Makes it clear where the parameter comes from
)
```

---

## Testing the API

### Step 1: Access Swagger UI
Navigate to: http://localhost:8082/swagger-ui.html

### Step 2: Test Public Endpoints
Try these without authentication:
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/products` - View all products

### Step 3: Authenticate
1. Copy the JWT token from login response
2. Click **"Authorize"** button (🔒)
3. Enter: `Bearer your-token-here`
4. Click **"Authorize"**

### Step 4: Test Protected Endpoints
Now you can test:
- `GET /api/auth/me` - Get your profile
- `POST /api/products` - Create a product
- `GET /api/products/my` - Get your products
- `PUT /api/products/{id}` - Update your product
- `DELETE /api/products/{id}` - Delete your product

### Step 5: Test Admin Endpoints
1. Register an admin account using admin secret
2. Login as admin
3. Authorize with admin JWT token
4. Test admin endpoints:
   - `GET /api/users` - View all users
   - `DELETE /api/users/{id}` - Delete a user

---

## Common Issues & Solutions

### Issue 1: "Unauthorized" on protected endpoints
**Solution**: Make sure you clicked "Authorize" and entered your JWT token correctly

### Issue 2: Swagger UI not loading
**Solution**: Check that `/swagger-ui/**` and `/v3/api-docs/**` are permitted in SecurityConfig

### Issue 3: Request body schema not showing
**Solution**: Verify DTOs have proper `@Schema` annotations

### Issue 4: Example values not appearing
**Solution**: Add `example` attribute to `@Parameter` or `@Schema` annotations

---

## Summary

### Key Components
1. **Springdoc OpenAPI** - Auto-generates API docs
2. **Annotations** - Document endpoints, parameters, responses
3. **Security Integration** - JWT authentication in Swagger UI
4. **Interactive Testing** - Test APIs directly from browser

### Annotations Used
- `@Tag` - Group endpoints
- `@Operation` - Document endpoints
- `@Parameter` - Document parameters
- `@ApiResponse` - Document responses
- `@RequestBody` (Swagger) - Document request bodies
- `@SecurityRequirement` - Mark protected endpoints
- `@Schema` - Document DTOs
- `@Content` - Specify content types

### Best Practice Pattern
```java
@Tag(name = "Resource", description = "Resource management")
public class ResourceController {
    
    @Operation(
            summary = "Short description",
            description = "Detailed description",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(...),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getResource(
            @Parameter(
                    name = "id",
                    description = "Resource ID",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long id) {
        // Implementation
    }
}
```

---

## Additional Resources

- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Springdoc Documentation](https://springdoc.org/)
- [Swagger UI Guide](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations)

---

**Last Updated**: January 26, 2026
**Version**: 1.0.0
**Author**: Development Team
