# User Authentication and Authorization System

A Spring Boot REST API providing JWT-based authentication, role-based authorization, user management, and OTP password reset functionality.

## 🚀 Features

### Authentication & Authorization
- JWT-based stateless authentication
- BCrypt password encryption
- Role-based access control (ADMIN, USER)
- Login with username or email
- Token-based authentication filter

### User Management
- Public user registration
- Admin CRUD operations for users
- User profile management (view, update, patch)
- User enable/disable functionality

### Password Reset
- OTP-based password reset flow
- 6-digit OTP generation
- 5-minute OTP expiration
- Single-use OTP validation
- Mock email service (console logging)

## 🛠 Technology Stack

- **Java**: 21
- **Spring Boot**: 3.4.2
- **Spring Security**: 6.x
- **Spring Data JPA**: Hibernate ORM
- **Database**: MySQL 8.x (H2 for testing)
- **JWT**: jjwt 0.11.5
- **MapStruct**: 1.5.5 (Object mapping)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Coverage**: JaCoCo (90% minimum enforced)
- **Utilities**: Lombok, OpenAPI/Swagger

## 📋 Prerequisites

- Java 21+
- Maven 3.6+
- MySQL 8.x
- IDE (IntelliJ IDEA recommended)

## 🏃 Getting Started

### 1. Configure MySQL Database

Create a MySQL database:

```sql
CREATE DATABASE auth_system;
```

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_system
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints (Public)

#### Register
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Request Password Reset
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "john@example.com",
  "otpCode": "123456",
  "newPassword": "newPassword123"
}
```

#### Validate OTP
```http
POST /api/auth/validate-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otpCode": "123456"
}
```

### User Endpoints (Authenticated)

All endpoints require JWT token:
```
Authorization: Bearer <your-jwt-token>
```

#### Get Current User
```http
GET /api/users/me
```

#### Update Current User (Full)
```http
PUT /api/users/me
Content-Type: application/json

{
  "username": "newusername",
  "email": "newemail@example.com"
}
```

#### Update Current User (Partial)
```http
PATCH /api/users/me
Content-Type: application/json

{
  "username": "updatedusername"
}
```

### Admin Endpoints (ADMIN Role Required)

#### Get All Users
```http
GET /api/users
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Update User
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "updatedusername",
  "email": "updated@example.com"
}
```

#### Delete User
```http
DELETE /api/users/{id}
```

#### Disable User
```http
PATCH /api/users/{id}/disable
```

#### Enable User
```http
PATCH /api/users/{id}/enable
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

View in browser: `target/site/jacoco/index.html`

### Test Coverage
- **90%+ required** (enforced by JaCoCo)
- **97 total tests**
- Unit tests for all services
- Integration tests for all controllers
- Edge case and branch coverage tests

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/auth/
│   │   ├── config/          # Security configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions & handlers
│   │   ├── mapper/         # MapStruct mappers
│   │   ├── repository/     # JPA repositories
│   │   ├── security/       # JWT provider, filters, user details
│   │   └── service/        # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/auth/
        ├── controller/     # Integration tests
        ├── dto/           # DTO validation tests
        ├── exception/     # Exception handler tests
        ├── security/      # Security component tests
        └── service/       # Service unit tests
```

## ⚙️ Configuration

### JWT Settings

Edit `application.properties`:

```properties
app.jwt.secret=your-base64-encoded-secret-key
app.jwt.expiration-milliseconds=86400000
```

### Database Schema

Tables are auto-created via JPA:

- **users**: User accounts with roles
- **password_reset_otp**: OTP tokens for password reset

## 🔒 Security

- **Password Hashing**: BCrypt (strength 10)
- **JWT Signing**: HS256 algorithm
- **Session Management**: Stateless
- **CSRF Protection**: Disabled (stateless API)
- **Token Expiration**: 24 hours (configurable)
- **OTP Expiration**: 5 minutes

## 🎯 Validation Rules

- **Username**: Required, unique, no '@' symbol
- **Email**: Required, unique, valid format
- **Password**: Minimum 8 characters
- **OTP**: 6 digits, single-use

## 👤 Default Roles

- **USER**: Auto-assigned to new registrations
- **ADMIN**: Must be manually set in database

To create an admin:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'adminuser';
```

## 🚨 Error Responses

All errors return JSON:
```json
{
  "timestamp": "2026-02-02T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error description",
  "path": "/api/endpoint"
}
```

**HTTP Status Codes:**
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Authentication failed
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## 🏗️ Architecture Highlights

### MapStruct Integration
Object mapping is handled by MapStruct for type-safe, compile-time DTO/Entity conversions:
- `UserMapper`: User ↔ UserDto, RegisterRequest → User
- `OtpMapper`: PasswordResetOtp creation

### JWT Authentication Flow
1. User logs in with credentials
2. System validates and generates JWT with User ID as subject
3. Client includes JWT in Authorization header
4. `JwtAuthenticationFilter` validates token on each request
5. Security context is populated with user details

## 📦 Build Production JAR

```bash
mvn clean package
java -jar target/auth-0.0.1-SNAPSHOT.jar
```

## 📄 License

This project is for educational purposes.
