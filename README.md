# SimpleSpringBootCRUD

A full-stack web application built with Spring Boot and React that provides a comprehensive CRUD (Create, Read, Update, Delete) system for product management with user authentication, role-based access control, and supplier management features.

## 📋 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Database Schema](#database-schema)

## ✨ Features

### User Management
- **User Registration & Authentication**: Secure signup and login with JWT token-based authentication
- **Role-Based Access Control**: Support for multiple user roles (USER, ADMIN, SUPPLIER)
- **User Profile Management**: View and update user information
- **Admin User Management**: Admins can view, update, and delete users

### Product Management
- **Product CRUD Operations**: Create, read, update, and delete products
- **Image Upload**: Support for product image uploads (up to 5MB)
- **Product Search**: Search products by name or description
- **Stock Management**: Track product stock quantities and identify low-stock items
- **Supplier Association**: Products can be linked to supplier accounts

### Supplier Features
- **Supplier Applications**: Users can apply to become suppliers
- **Supplier Dashboard**: Dedicated dashboard for supplier accounts
- **Supplier Product Management**: Suppliers can manage their own products
- **Application Review**: Admins can review and approve/reject supplier applications

### Admin Features
- **Admin Authentication**: Secure admin signup with secret key
- **User Management**: View and manage all users in the system
- **Supplier Management**: Review supplier applications and manage supplier accounts
- **Full Product Access**: View and manage all products across all suppliers

### Security Features
- **JWT Authentication**: Stateless token-based authentication
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Configured for secure cross-origin requests
- **Role-Based Authorization**: Endpoint protection based on user roles

### Additional Features
- **Logging System**: Comprehensive logging with Logback
- **Database Migrations**: Flyway support for version-controlled database schema
- **MapStruct Integration**: Efficient DTO-Entity mapping
- **Validation**: Input validation using Spring Boot Validation
- **File Upload Management**: Organized file storage for product images

## 🛠 Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.2.1**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Boot Validation
- **MySQL Database**
- **JWT (JSON Web Tokens)** - v0.11.5
- **Flyway** - Database migrations
- **MapStruct** - v1.5.5.Final (DTO mapping)
- **Lombok** - v1.18.30 (Boilerplate reduction)
- **Logback** - Logging framework
- **Maven** - Build tool

### Frontend
- **React 18.2.0**
- **React Router DOM 6.26.2** - Client-side routing
- **Axios 1.7.2** - HTTP client
- **React Scripts 5.0.1** - Build tooling

## 📦 Prerequisites

Before running this application, ensure you have the following installed:

### Required Software
- **Java Development Kit (JDK) 21** or higher
  - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
- **Maven 3.6+**
  - Download from: [Apache Maven](https://maven.apache.org/download.cgi)
- **Node.js 16+** and **npm**
  - Download from: [Node.js](https://nodejs.org/)
- **MySQL 5.7+** or **MySQL 8.0+**
  - Download from: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

### Environment Setup
1. Verify Java installation:
   ```bash
   java -version
   ```

2. Verify Maven installation:
   ```bash
   mvn -version
   ```

3. Verify Node.js and npm installation:
   ```bash
   node -v
   npm -v
   ```

4. Verify MySQL installation:
   ```bash
   mysql --version
   ```

## 🚀 Installation & Setup

Running this project with intellij Pro should automatically start the application and create tables on database(I used Xampp which should be running(apache,mysql) to run this project)
### 1. Clone the Repository
```bash
git clone <repository-url>
cd SimpleSpringBootCRUD
```
### Manual setup
### 2. Database Setup

#### Create MySQL Database
```sql
CREATE DATABASE crud_db;
```

#### Configure Database Connection
Update the database credentials in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

#### Database Migrations (Optional)
If using MySQL 5.7+, enable Flyway for automatic schema migrations:

```properties
spring.flyway.enabled=true
```

Otherwise, manually run the migration scripts located in `backend/src/main/resources/db/migration/`:
- `V1__init.sql` - Initial schema
- `V2__supplier_feature.sql` - Supplier features
- `V3__fix_role_column.sql` - Role column adjustments

### 3. Backend Setup

Navigate to the backend directory:
```bash
cd backend
```

#### Install Dependencies
Maven will automatically download dependencies during build. To explicitly download:
```bash
mvn clean install
```

#### Configure Application Settings
Review and update `backend/src/main/resources/application.properties`:

- **Server Port** (default: 8082)
- **JWT Secret Key** (change for production)
- **Admin Secret** (for admin registration)
- **File Upload Directory** (default: uploads)

### 4. Frontend Setup

Navigate to the frontend directory:
```bash
cd frontend
```

#### Install Dependencies
```bash
npm install
```

#### Configure API Endpoint
The frontend is pre-configured to communicate with the backend at `http://localhost:8082`. If you change the backend port, update the API base URL in `frontend/src/services/api.js`.

## 🎯 Running the Application

### Start the Backend Server

From the `backend` directory:

```bash
mvn spring-boot:run
```

Or build and run the JAR:
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The backend server will start on **http://localhost:8082**

### Start the Frontend Development Server

From the `frontend` directory:

```bash
npm start
```

The frontend will start on **http://localhost:3000** and automatically open in your default browser.

### Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8082/api

### Default Admin Account

To create an admin account, use the admin signup endpoint with the admin secret key configured in `application.properties`:

```json
POST http://localhost:8082/api/auth/admin/signup
{
  "username": "admin",
  "email": "admin@example.com",
  "password": "adminpassword",
  "adminSecret": "ADMIN"
}
```

## 📁 Project Structure

```
SimpleSpringBootCRUD/
├── backend/                          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/simple/crud/demo/
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   ├── mapper/           # MapStruct DTOs mappers
│   │   │   │   ├── model/            # JPA Entities
│   │   │   │   ├── repository/       # JPA Repositories
│   │   │   │   ├── security/         # Security & JWT components
│   │   │   │   ├── service/          # Business logic layer
│   │   │   │   └── DemoApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── logback-spring.xml
│   │   │       └── db/migration/     # Flyway migrations
│   │   └── test/                     # Unit tests
│   ├── uploads/                      # Product image storage
│   ├── logs/                         # Application logs
│   └── pom.xml                       # Maven dependencies
│
└── frontend/                         # React Frontend
    ├── public/
    │   └── index.html
    ├── src/
    │   ├── components/
    │   │   └── ProtectedRoute.jsx    # Route protection
    │   ├── pages/                    # Page components
    │   │   ├── AdminLogin.jsx
    │   │   ├── AdminSignup.jsx
    │   │   ├── AdminSuppliers.jsx
    │   │   ├── AdminUsers.jsx
    │   │   ├── Home.jsx
    │   │   ├── Login.jsx
    │   │   ├── ProductForm.jsx
    │   │   ├── Products.jsx
    │   │   ├── Signup.jsx
    │   │   └── SupplierHub.jsx
    │   ├── services/                 # API service layer
    │   │   ├── api.js
    │   │   ├── auth.js
    │   │   ├── products.js
    │   │   └── supplier.js
    │   ├── App.js
    │   └── index.js
    └── package.json                  # npm dependencies
```

## 🔌 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/signup` | Register new user | Public |
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/admin/signup` | Admin registration | Public (requires secret) |
| POST | `/api/auth/admin/login` | Admin login | Public |

### User Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users` | Get all users | Admin |
| GET | `/api/users/{id}` | Get user by ID | Admin |
| GET | `/api/users/username/{username}` | Get user by username | Admin |
| PUT | `/api/users/{id}` | Update user | Admin |
| DELETE | `/api/users/{id}` | Delete user | Admin |

### Product Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/products` | Get all products | Authenticated |
| GET | `/api/products/{id}` | Get product by ID | Authenticated |
| GET | `/api/products/search` | Search products | Authenticated |
| GET | `/api/products/in-stock` | Get in-stock products | Authenticated |
| GET | `/api/products/low-stock` | Get low-stock products | Admin/Supplier |
| GET | `/api/products/my` | Get user's products | User |
| GET | `/api/products/supplied` | Get supplier's products | Supplier |
| POST | `/api/products` | Create product | Admin/Supplier |
| PUT | `/api/products/{id}` | Update product | Admin/Supplier |
| DELETE | `/api/products/{id}` | Delete product | Admin/Supplier |

### Supplier Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/suppliers/applications` | Apply to become supplier | User |
| GET | `/api/suppliers/applications/me` | Get my applications | User |
| GET | `/api/suppliers/dashboard` | Get supplier dashboard | Supplier |

### Admin Supplier Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/admin/suppliers/applications` | Get all applications | Admin |
| GET | `/api/admin/suppliers/applications/{id}` | Get application details | Admin |
| PUT | `/api/admin/suppliers/applications/{id}/approve` | Approve application | Admin |
| PUT | `/api/admin/suppliers/applications/{id}/reject` | Reject application | Admin |

## ⚙️ Configuration

### Backend Configuration (`application.properties`)

#### Server Settings
```properties
server.port=8082
```

#### Database Settings
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud_db
spring.datasource.username=root
spring.datasource.password=your_password
```

#### JWT Settings
```properties
app.jwt.secret=your_secret_key_here
app.jwt.expiration-ms=86400000
```

#### Admin Settings
```properties
app.admin.secret=your_admin_secret
```

#### File Upload Settings
```properties
app.uploads.dir=uploads
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
app.uploads.max-image-size=5242880
```

#### CORS Settings
```properties
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    supplier_since DATETIME NULL,
    supplier_profile VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME
);
```

### Products Table
```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL,
    supplier_id BIGINT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (supplier_id) REFERENCES users(id)
);
```

### Supplier Applications Table
```sql
CREATE TABLE supplier_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL,
    business_name VARCHAR(150) NOT NULL,
    business_email VARCHAR(255) NOT NULL,
    business_phone VARCHAR(50),
    website VARCHAR(255),
    message TEXT,
    status VARCHAR(20) NOT NULL,
    submitted_at DATETIME NOT NULL,
    reviewed_at DATETIME,
    reviewed_by VARCHAR(100),
    admin_note VARCHAR(500),
    FOREIGN KEY (applicant_id) REFERENCES users(id)
);
```

## 📝 Logging

The application uses Logback for comprehensive logging:
- Log files are stored in the `backend/logs/` directory
- Log levels can be configured in `logback-spring.xml`
- Separate logs for application and access logs

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is available for use as per the repository license.

## 📧 Support

For issues, questions, or contributions, please open an issue on the repository.

---

**Built with ❤️ using Spring Boot and React**
