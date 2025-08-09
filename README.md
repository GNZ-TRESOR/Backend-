# 🏥 Ubuzima Backend API

## 📋 Overview

This is the backend API for the Ubuzima Family Planning Mobile Application, built with Spring Boot 3.2.2 and Java 17.

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **PostgreSQL 12** or higher
- **Maven 3.6** or higher (or use included wrapper)

### Database Setup

1. **Create Database:**
```sql
CREATE DATABASE ubuzima_db;
```

2. **Update Configuration:**
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ubuzima_db
    username: your_username
    password: your_password
```

### Running the Application

#### Option 1: Using Maven Wrapper (Recommended)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Option 2: Using Maven
```bash
mvn spring-boot:run
```

#### Option 3: Using IDE
1. Import project into IntelliJ IDEA or Eclipse
2. Run `UbuzimaApplication.java`

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v1/v3/api-docs
- **Health Check**: http://localhost:8080/api/v1/health

## 🔐 Authentication

The API uses JWT tokens for authentication. Default test users:

- **Admin**: `admin@ubuzima.rw` / `admin123`
- **Health Worker**: `healthworker@ubuzima.rw` / `healthworker123`
- **Client**: `client@ubuzima.rw` / `client123`

## 🛠 API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout

### Admin APIs
- `GET /admin/users` - Get all users
- `GET /admin/dashboard/stats` - System statistics
- `PUT /admin/users/{id}/status` - Update user status

### Health Worker APIs
- `GET /health-worker/{id}/clients` - Get assigned clients
- `GET /health-worker/{id}/appointments` - Get appointments
- `PUT /health-worker/appointments/{id}/status` - Update appointment

### Client APIs
- `GET /client/{id}/profile` - Get profile
- `GET /client/{id}/appointments` - Get appointments
- `POST /client/{id}/appointments` - Book appointment
- `GET /client/{id}/health-records` - Get health records

### Facilities
- `GET /facilities` - Get all facilities
- `GET /facilities/nearby` - Find nearby facilities
- `POST /facilities` - Create facility

## 🗄 Database Schema

The application automatically creates the following tables:

- `users` - User accounts and profiles
- `health_records` - Health tracking data
- `appointments` - Appointment scheduling
- `health_facilities` - Health facility information

## 🔧 Configuration

### Profiles

- **dev** - Development (default)
- **test** - Testing with H2 database
- **prod** - Production

### Environment Variables

For production, set these environment variables:

```bash
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
JWT_SECRET=your-secret-key
```

## 🧪 Testing

### Run Tests
```bash
# All tests
.\mvnw.cmd test

# Specific test
.\mvnw.cmd test -Dtest=UserServiceTest
```

### Test Coverage
```bash
.\mvnw.cmd jacoco:report
```

## 📦 Building

### Create JAR
```bash
.\mvnw.cmd clean package
```

### Skip Tests
```bash
.\mvnw.cmd clean package -DskipTests
```

## 🐳 Docker

### Build Image
```bash
docker build -t ubuzima-backend .
```

### Run Container
```bash
docker run -p 8080:8080 ubuzima-backend
```

## 🔍 Monitoring

### Health Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Logging

Logs are configured for different levels:
- **DEBUG** - Development details
- **INFO** - General information
- **WARN** - Warnings
- **ERROR** - Errors only

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check PostgreSQL is running
   - Verify credentials in application.yml
   - Ensure database exists

2. **Port Already in Use**
   - Change port in application.yml: `server.port=8081`
   - Or kill process using port 8080

3. **Java Version Issues**
   - Ensure Java 17+ is installed
   - Set JAVA_HOME environment variable

### Getting Help

1. Check application logs
2. Verify database connectivity
3. Test with Swagger UI
4. Check GitHub issues

## 📄 License

This project is licensed under the MIT License.

## 👥 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## 📞 Support

For support, email dev@ubuzima.rw or create an issue on GitHub.
