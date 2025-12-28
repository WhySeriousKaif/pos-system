# Molla POS System - Complete Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Core Components](#core-components)
6. [Security Implementation](#security-implementation)
7. [API Endpoints](#api-endpoints)
8. [Database Schema](#database-schema)
9. [Request/Response Examples](#requestresponse-examples)
10. [Setup Instructions](#setup-instructions)

---

## Project Overview

**Molla POS System** is a Point of Sale (POS) system built with Spring Boot, implementing JWT-based authentication and role-based access control. The system provides secure user registration and login functionality with support for multiple user roles.

### Key Features
- ✅ JWT-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Password encryption using BCrypt
- ✅ RESTful API design
- ✅ MySQL database integration
- ✅ CORS configuration for frontend integration

---

## Architecture & Design Patterns

### Architecture Pattern
The project follows a **Layered Architecture** pattern:

```
┌─────────────────────────────────────┐
│      Controllers (REST API)          │
├─────────────────────────────────────┤
│      Services (Business Logic)       │
├─────────────────────────────────────┤
│      Repositories (Data Access)      │
├─────────────────────────────────────┤
│      Models (Entities)                │
└─────────────────────────────────────┘
```

### Design Patterns Used

1. **Repository Pattern**: Data access abstraction through Spring Data JPA
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: Data Transfer Objects for API communication
4. **Mapper Pattern**: Conversion between Entity and DTO
5. **Filter Pattern**: JWT validation through custom filter
6. **Strategy Pattern**: Password encoding strategies

---

## Technology Stack

### Backend Framework
- **Spring Boot 3.5.9**: Main framework
- **Java 17**: Programming language

### Security
- **Spring Security 6.5.7**: Authentication and authorization
- **JWT (JJWT 0.11.5)**: Token-based authentication
- **BCrypt**: Password hashing

### Database
- **MySQL**: Relational database
- **Spring Data JPA**: ORM framework
- **Hibernate**: JPA implementation

### Build Tool
- **Maven**: Dependency management and build tool

### Other Libraries
- **Lombok**: Reduces boilerplate code
- **Jakarta Validation**: Input validation
- **Jackson**: JSON processing

---

## Project Structure

```
molla-pos-system/
├── src/main/java/com/molla/
│   ├── configuration/          # Security & JWT configuration
│   │   ├── SecurityConfig.java
│   │   ├── JwtProvider.java
│   │   ├── JwtValidator.java
│   │   └── JwtConstant.java
│   ├── controllers/            # REST API endpoints
│   │   └── AuthController.java
│   ├── domain/                 # Domain enums
│   │   └── UserRole.java
│   ├── exceptions/             # Custom exceptions
│   │   └── UserException.java
│   ├── mapper/                 # Entity-DTO mappers
│   │   └── UserMapper.java
│   ├── model/                  # JPA entities
│   │   └── User.java
│   ├── payload/                # DTOs and responses
│   │   ├── dto/
│   │   │   └── UserDto.java
│   │   └── response/
│   │       └── AuthResponse.java
│   ├── repository/             # Data access layer
│   │   └── UserRepository.java
│   └── service/                # Business logic
│       ├── AuthService.java
│       └── impl/
│           ├── AuthServiceImp.java
│           └── CustomUserImplementation.java
└── src/main/resources/
    └── application.properties  # Configuration
```

---

## Core Components

### 1. Model Layer (`model/`)

#### User Entity
**File**: `com.molla.model.User`

Represents the user entity in the database.

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String phone;
    
    @Column(nullable = false)
    private UserRole role;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
```

**Key Annotations:**
- `@Entity`: Marks as JPA entity
- `@Id`: Primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Auto-increment ID
- `@Email`: Email validation
- `@Column`: Database column configuration

---

### 2. Domain Layer (`domain/`)

#### UserRole Enum
**File**: `com.molla.domain.UserRole`

Defines available user roles in the system.

```java
public enum UserRole {
    ROLE_ADMIN,           // System administrator
    ROLE_USER,            // Regular user
    ROLE_CASHIER,         // Cashier role
    ROLE_BRANCH_MANAGER,  // Branch manager
    ROLE_STORE_MANAGER    // Store manager
}
```

**Note**: Roles must start with `ROLE_` prefix for Spring Security compatibility.

---

### 3. Repository Layer (`repository/`)

#### UserRepository
**File**: `com.molla.repository.UserRepository`

Extends Spring Data JPA's `JpaRepository` for database operations.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

**Methods:**
- `findByEmail(String email)`: Custom query method to find user by email
- Inherited methods: `save()`, `findById()`, `findAll()`, `delete()`, etc.

---

### 4. Service Layer (`service/`)

#### AuthService Interface
**File**: `com.molla.service.AuthService`

Defines authentication service contract.

```java
public interface AuthService {
    AuthResponse signUp(UserDto user) throws UserException;
    AuthResponse login(UserDto user);
}
```

#### AuthServiceImp Implementation
**File**: `com.molla.service.impl.AuthServiceImp`

**Sign Up Process:**
1. Check if user already exists
2. Validate role (prevent ADMIN registration)
3. Encode password using BCrypt
4. Save user to database
5. Generate JWT token
6. Return authentication response

**Login Process:**
1. Authenticate user credentials
2. Generate JWT token
3. Update last login timestamp
4. Return authentication response

**Key Dependencies:**
- `UserRepository`: Database operations
- `PasswordEncoder`: Password hashing
- `JwtProvider`: Token generation
- `CustomUserImplementation`: User details loading

#### CustomUserImplementation
**File**: `com.molla.service.impl.CustomUserImplementation`

Implements Spring Security's `UserDetailsService` to load user details.

```java
@Service
public class CustomUserImplementation implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        // 1. Fetch user from database
        User user = userRepository.findByEmail(username);
        
        // 2. Check if user exists
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        
        // 3. Convert role to GrantedAuthority
        GrantedAuthority authority = 
            new SimpleGrantedAuthority(user.getRole().toString());
        
        // 4. Return Spring Security User object
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(authority)
        );
    }
}
```

---

### 5. Controller Layer (`controllers/`)

#### AuthController
**File**: `com.molla.controllers.AuthController`

Handles HTTP requests for authentication endpoints.

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUpHandler(
            @RequestBody UserDto userDto) throws UserException {
        return new ResponseEntity<>(
            authService.signUp(userDto), 
            HttpStatus.CREATED
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(
            @RequestBody UserDto userDto) {
        return new ResponseEntity<>(
            authService.login(userDto), 
            HttpStatus.OK
        );
    }
}
```

**Annotations:**
- `@RestController`: Combines `@Controller` and `@ResponseBody`
- `@RequestMapping`: Base URL mapping
- `@PostMapping`: HTTP POST endpoint
- `@RequestBody`: Deserializes JSON to Java object

---

### 6. DTO Layer (`payload/`)

#### UserDto
**File**: `com.molla.payload.dto.UserDto`

Data Transfer Object for user data.

```java
@Data
public class UserDto {
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
```

**Purpose**: Separates API contract from entity model.

#### AuthResponse
**File**: `com.molla.payload.response.AuthResponse`

Response object for authentication endpoints.

```java
@Data
public class AuthResponse {
    private String jwt;
    private String message;
    private UserDto user;
}
```

---

### 7. Mapper Layer (`mapper/`)

#### UserMapper
**File**: `com.molla.mapper.UserMapper`

Converts between `User` entity and `UserDto`.

```java
public class UserMapper {
    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setFullName(user.getFullName());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setRole(user.getRole());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        userDto.setLastLoginAt(user.getLastLoginAt());
        return userDto;
    }
}
```

**Note**: Password is intentionally excluded from DTO for security.

---

### 8. Exception Handling (`exceptions/`)

#### UserException
**File**: `com.molla.exceptions.UserException`

Custom exception for user-related errors.

```java
public class UserException extends Throwable {
    public UserException(String message) {
        super(message);
    }
}
```

**Usage Examples:**
- User already exists
- Cannot register as ADMIN
- Invalid credentials

---

## Security Implementation

### 1. Security Configuration

**File**: `com.molla.configuration.SecurityConfig`

#### Key Features:

**Stateless Session Management:**
```java
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```
- No server-side sessions
- JWT tokens used for authentication

**Authorization Rules:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/super-admin/**").hasRole("ADMIN")
    .requestMatchers("/api/**").authenticated()
    .anyRequest().permitAll()
)
```

**CORS Configuration:**
```java
config.setAllowedOrigins(List.of(
    "http://localhost:3000",  // React default
    "http://localhost:5173"   // Vite default
));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);
```

**Password Encoder:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

### 2. JWT Provider

**File**: `com.molla.configuration.JwtProvider`

#### Token Generation Process:

```java
public String generateToken(Authentication authentication) {
    // 1. Extract user roles
    Collection<? extends GrantedAuthority> authorities = 
        authentication.getAuthorities();
    String roles = populateAuthorities(authorities);
    
    // 2. Build JWT token
    return Jwts.builder()
        .setSubject(authentication.getName())  // Email
        .claim("role", roles)                   // User roles
        .setIssuedAt(new Date())                // Creation time
        .setExpiration(new Date(
            System.currentTimeMillis() + 86400000  // 24 hours
        ))
        .signWith(key)                          // Sign with secret key
        .compact();
}
```

#### Token Structure:
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}
Payload: {
  "sub": "user@example.com",
  "role": "ROLE_USER",
  "iat": 1234567890,
  "exp": 1234654290
}
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

---

### 3. JWT Validator Filter

**File**: `com.molla.configuration.JwtValidator`

#### Validation Process:

```java
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException {
    
    // 1. Extract Authorization header
    String authHeader = request.getHeader("Authorization");
    
    // 2. Check if token exists
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        
        // 3. Parse and validate token
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        // 4. Extract user details
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        
        // 5. Create authentication object
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                email, null, 
                List.of(new SimpleGrantedAuthority(role))
            );
        
        // 6. Set in security context
        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }
    
    // 7. Continue filter chain
    filterChain.doFilter(request, response);
}
```

**Filter Chain Position:**
- Added before `BasicAuthenticationFilter`
- Executes on every request
- Validates JWT token from Authorization header

---

### 4. JWT Constants

**File**: `com.molla.configuration.JwtConstant`

```java
public class JwtConstant {
    public static final String JWT_SECRET = 
        "KAIF0786@njnskjnsjkfnkjfnkjfnkfnksnfkfn";
    public static final String JWT_HEADERS = "Authorization";
}
```

**⚠️ Security Note**: In production, store JWT_SECRET in environment variables or secure configuration.

---

## API Endpoints

### Base URL
```
http://localhost:5001
```

### Authentication Endpoints

#### 1. User Registration
**Endpoint**: `POST /auth/signup`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "phone": "+1234567890",
  "role": "ROLE_USER"
}
```

**Response (201 Created):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "User registered successfully",
  "user": {
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "role": "ROLE_USER",
    "createdAt": "2025-12-28T12:00:00",
    "updatedAt": "2025-12-28T12:00:00",
    "lastLoginAt": "2025-12-28T12:00:00"
  }
}
```

**Error Responses:**
- `400 Bad Request`: User already exists
- `400 Bad Request`: Cannot register as ADMIN

---

#### 2. User Login
**Endpoint**: `POST /auth/login`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successfully",
  "user": {
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "role": "ROLE_USER",
    "createdAt": "2025-12-28T12:00:00",
    "updatedAt": "2025-12-28T12:00:00",
    "lastLoginAt": "2025-12-28T12:30:00"
  }
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials

---

### Protected Endpoints

#### Using JWT Token
For protected endpoints, include JWT token in Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example Protected Endpoint:**
```
GET /api/users/profile
Authorization: Bearer <token>
```

---

## Database Schema

### User Table

```sql
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `full_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(255),
    `role` VARCHAR(50) NOT NULL,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    `last_login_at` DATETIME,
    PRIMARY KEY (`id`)
);
```

### Field Descriptions:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `full_name` | VARCHAR(255) | NOT NULL | User's full name |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | User's email (used for login) |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `phone` | VARCHAR(255) | NULLABLE | User's phone number |
| `role` | VARCHAR(50) | NOT NULL | User role (enum) |
| `created_at` | DATETIME | NULLABLE | Account creation timestamp |
| `updated_at` | DATETIME | NULLABLE | Last update timestamp |
| `last_login_at` | DATETIME | NULLABLE | Last login timestamp |

### Indexes:
- Primary Key: `id`
- Unique Index: `email`

---

## Request/Response Examples

### Example 1: User Registration

**cURL Command:**
```bash
curl -X POST http://localhost:5001/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Smith",
    "email": "jane.smith@example.com",
    "password": "MySecurePass123!",
    "phone": "+1987654321",
    "role": "ROLE_CASHIER"
  }'
```

**JavaScript (Fetch API):**
```javascript
const response = await fetch('http://localhost:5001/auth/signup', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    fullName: 'Jane Smith',
    email: 'jane.smith@example.com',
    password: 'MySecurePass123!',
    phone: '+1987654321',
    role: 'ROLE_CASHIER'
  })
});

const data = await response.json();
console.log('JWT Token:', data.jwt);
localStorage.setItem('token', data.jwt);
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqYW5lLnNtaXRoQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfQ0FTSElFUiIsImlhdCI6MTczNTM4NDAwMCwiZXhwIjoxNzM1NDcwNDAwfQ...",
  "message": "User registered successfully",
  "user": {
    "fullName": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "+1987654321",
    "role": "ROLE_CASHIER",
    "createdAt": "2025-12-28T12:00:00",
    "updatedAt": "2025-12-28T12:00:00",
    "lastLoginAt": "2025-12-28T12:00:00"
  }
}
```

---

### Example 2: User Login

**cURL Command:**
```bash
curl -X POST http://localhost:5001/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@example.com",
    "password": "MySecurePass123!"
  }'
```

**JavaScript (Fetch API):**
```javascript
const response = await fetch('http://localhost:5001/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'jane.smith@example.com',
    password: 'MySecurePass123!'
  })
});

const data = await response.json();
if (response.ok) {
  localStorage.setItem('token', data.jwt);
  console.log('Login successful!');
} else {
  console.error('Login failed');
}
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqYW5lLnNtaXRoQGV4YW1wbGUuY29tIiwicm9sZSI6IlJPTEVfQ0FTSElFUiIsImlhdCI6MTczNTM4NDMwMCwiZXhwIjoxNzM1NDcwNzAwfQ...",
  "message": "Login successfully",
  "user": {
    "fullName": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "+1987654321",
    "role": "ROLE_CASHIER",
    "createdAt": "2025-12-28T12:00:00",
    "updatedAt": "2025-12-28T12:00:00",
    "lastLoginAt": "2025-12-28T12:30:00"
  }
}
```

---

### Example 3: Using JWT Token for Protected Endpoints

**JavaScript:**
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:5001/api/users/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

if (response.ok) {
  const data = await response.json();
  console.log('User profile:', data);
} else if (response.status === 401) {
  console.error('Token expired or invalid');
  localStorage.removeItem('token');
  // Redirect to login
}
```

---

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Step 1: Clone and Navigate
```bash
cd /Users/mdkaif/Downloads/molla-pos-system
```

### Step 2: Database Setup

1. **Create Database:**
```sql
CREATE DATABASE pos_db;
```

2. **Update Database Credentials** in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pos_db
spring.datasource.username=root
spring.datasource.password=your_password
```

3. **Fix User Table** (if needed):
```sql
USE pos_db;
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT;
```

### Step 3: Build Project
```bash
mvn clean install
```

### Step 4: Run Application
```bash
mvn spring-boot:run
```

Or run from IDE:
- Right-click `MollaPosSystemApplication.java`
- Select "Run"

### Step 5: Verify
- Application starts on `http://localhost:5001`
- Check logs for: "Started MollaPosSystemApplication"

---

## Configuration Files

### application.properties

```properties
# Application
spring.application.name=molla-pos-system
server.port=5001

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pos_db
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

**Key Settings:**
- `ddl-auto=update`: Automatically updates database schema
- `show-sql=true`: Logs SQL queries (useful for debugging)

---

## Security Best Practices

### ✅ Implemented
- Password hashing with BCrypt
- JWT token expiration (24 hours)
- Role-based access control
- CORS configuration
- Stateless authentication

### ⚠️ Recommendations for Production

1. **JWT Secret Key:**
   - Move to environment variables
   - Use strong, randomly generated secret
   - Rotate periodically

2. **Password Policy:**
   - Enforce minimum length
   - Require special characters
   - Implement password strength validation

3. **Token Management:**
   - Implement refresh tokens
   - Add token blacklisting for logout
   - Reduce token expiration time

4. **HTTPS:**
   - Always use HTTPS in production
   - Never send tokens over HTTP

5. **Input Validation:**
   - Add comprehensive validation
   - Sanitize user inputs
   - Prevent SQL injection (already handled by JPA)

6. **Error Handling:**
   - Don't expose sensitive information in errors
   - Implement global exception handler
   - Log security events

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Error
**Error**: `Communications link failure`

**Solution**:
- Verify MySQL is running
- Check database credentials
- Ensure database exists

#### 2. JWT Token Invalid
**Error**: `Invalid JWT Token`

**Solution**:
- Check token expiration
- Verify JWT_SECRET matches
- Ensure token format: `Bearer <token>`

#### 3. User Already Exists
**Error**: `User already exists`

**Solution**:
- Use different email
- Delete existing user from database

#### 4. Field 'id' doesn't have a default value
**Error**: SQL exception on user creation

**Solution**:
```sql
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT;
```

---

## Development Workflow

### Adding New Features

1. **Create Entity** (`model/`)
2. **Create Repository** (`repository/`)
3. **Create DTO** (`payload/dto/`)
4. **Create Service** (`service/`)
5. **Create Controller** (`controllers/`)
6. **Update Security Config** (if needed)
7. **Test Endpoints**

### Code Style Guidelines

- Use Lombok annotations to reduce boilerplate
- Follow RESTful naming conventions
- Use meaningful variable names
- Add JavaDoc comments for public methods
- Keep methods focused and small

---

## Testing

### Manual Testing with cURL

**Sign Up:**
```bash
curl -X POST http://localhost:5001/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"Test123!","role":"ROLE_USER"}'
```

**Login:**
```bash
curl -X POST http://localhost:5001/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```

### Testing with Postman

1. Create new request
2. Set method to POST
3. Enter URL: `http://localhost:5001/auth/signup`
4. Go to Headers tab, add: `Content-Type: application/json`
5. Go to Body tab, select "raw" and "JSON"
6. Enter request body
7. Click Send

---

## Future Enhancements

### Planned Features
- [ ] Refresh token mechanism
- [ ] Email verification
- [ ] Password reset functionality
- [ ] User profile management
- [ ] Role management API
- [ ] Audit logging
- [ ] Rate limiting
- [ ] API documentation with Swagger/OpenAPI

---

## Conclusion

This documentation covers all aspects of the Molla POS System authentication module. The system implements secure JWT-based authentication with role-based access control, following Spring Boot best practices.

For questions or issues, refer to:
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Security Documentation: https://spring.io/projects/spring-security
- JWT.io: https://jwt.io/

---

**Last Updated**: December 28, 2025
**Version**: 1.0.0

