# ISE Policy Engine

A comprehensive AI-powered policy orchestration system built in Java that integrates with Cisco ISE to provide intelligent policy management with machine learning-driven recommendations, real-time network simulation, and automated policy enforcement.

This microservice provides a complete solution for intelligent network access control, combining traditional policy management with AI-driven insights to enhance security posture and automate policy decisions.

## 🏗️ Architecture Overview

The microservice follows a modular architecture with the following core components:

### 1. Policy Orchestrator
- Manages complete policy lifecycle (creation, update, rollback)
- Interfaces with ISE via REST APIs
- Receives AI recommendations and translates them into ISE-compatible policies
- Handles automatic and manual policy execution

### 2. AI/ML Engine
- Ingests contextual data (user, device, time, behavior)
- Calculates risk scores based on multiple factors
- Generates policy recommendations with confidence levels
- Detects anomalies in user and device behavior
- Maintains behavioral profiles for users and devices

### 3. Contextual Data Aggregator
- Pulls data from multiple sources (mocked):
  - Active Directory/LDAP
  - HR Systems
  - SIEM (Security Information and Event Management)
  - MDM (Mobile Device Management)
  - EDR/XDR (Endpoint Detection and Response)
  - Threat Intelligence feeds
- Normalizes and enriches session information
- Caches context data for performance

### 4. ISE Integration Layer (Mocked)
- Complete mock implementation of Cisco ISE APIs
- Supports policy management, session tracking, CoA operations
- Simulates real ISE responses and behavior
- Includes device management, security groups, and posture assessment

### 5. Policy Lifecycle Management
- Approval workflows (automatic and manual)
- Policy versioning and rollback capabilities
- Deployment to ISE with error handling
- Audit logging and compliance tracking

## 🚀 Features

- **Intelligent Risk Assessment**: Multi-factor risk scoring using time, location, device, user behavior, and threat intelligence
- **AI-Driven Policy Recommendations**: Automated policy suggestions based on risk analysis
- **Contextual Data Enrichment**: Real-time session enrichment from multiple data sources
- **Automated Policy Enforcement**: Automatic execution of low-risk policy changes
- **Change of Authorization (CoA)**: Support for ISE CoA operations (reauth, disconnect, reauthorize)
- **Behavioral Analytics**: User and device behavior profiling for anomaly detection
- **Policy Simulation**: Test policy changes before deployment
- **Comprehensive Audit Trail**: Complete logging of all policy actions and decisions
- **RESTful APIs**: Full REST API for integration and management
- **Security**: Spring Security with role-based access control

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (in-memory for development), PostgreSQL support
- **Caching**: Redis integration
- **Security**: Spring Security with HTTP Basic Authentication
- **AI Integration**: OpenAI GPT-4 for policy recommendations
- **HTTP Client**: Spring WebFlux for reactive HTTP calls
- **Testing**: JUnit 5, Spring Boot Test, Testcontainers
- **Build Tool**: Maven
- **Frontend**: React TypeScript (Admin Portal)
- **Documentation**: OpenAPI/Swagger (via Spring Boot Actuator)

## 📦 Project Structure

```
src/main/java/com/cisco/ise/ai/
├── ISEAIOrchestrator.java              # Main Spring Boot application
├── ai/                                  # AI/ML Engine components
│   ├── AIMLEngine.java                 # Main AI/ML service
│   ├── UserBehaviorProfile.java        # User behavior analytics
│   ├── DeviceBehaviorProfile.java      # Device behavior analytics
│   └── ...
├── config/                             # Configuration classes
│   ├── SecurityConfig.java            # Security configuration
│   └── ApplicationConfig.java         # General app configuration
├── context/                            # Contextual Data Aggregator
│   ├── ContextualDataAggregator.java  # Main aggregator service
│   ├── DataSourceConnector.java       # External data source connections
│   └── ...
├── dto/                                # Data Transfer Objects
│   ├── PolicyRecommendationRequest.java
│   ├── PolicyRecommendationResponse.java
│   └── ...
├── ise/                                # ISE Integration Layer
│   ├── client/
│   │   ├── ISEClient.java             # ISE client interface
│   │   └── MockISEClient.java         # Mocked ISE implementation
│   └── model/                         # ISE data models
├── model/                              # JPA Entity models
│   ├── Policy.java                    # Core policy entity
│   ├── Session.java                   # Network session entity
│   └── PolicyExecution.java           # Policy execution tracking
├── orchestrator/                       # Policy Orchestrator
│   ├── PolicyOrchestrator.java        # Main orchestrator service
│   ├── PolicyLifecycleManager.java    # Policy lifecycle management
│   └── controller/
│       └── PolicyController.java      # REST API endpoints
└── repository/                         # Data access layer
    ├── PolicyRepository.java
    └── PolicyExecutionRepository.java
```

## 🚦 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API Key (for AI-powered recommendations)
- (Optional) Redis for caching
- (Optional) PostgreSQL for production database
- (Optional) Node.js 16+ and npm (for admin portal)

### Environment Setup

1. **OpenAI API Key Configuration**

   The application requires an OpenAI API key for AI-powered policy recommendations. You can configure this in several ways:

   **Option 1: Environment Variable (Recommended)**
   ```bash
   export OPENAI_API_KEY=your-actual-openai-api-key-here
   ```

   **Option 2: Application Properties**
   Edit `src/main/resources/application.yml` and replace the placeholder:
   ```yaml
   openai:
     api:
       key: your-actual-openai-api-key-here
   ```

   **Option 3: System Property**
   ```bash
   mvn spring-boot:run -Dopenai.api.key=your-actual-openai-api-key-here
   ```

   > **Note**: Never commit your actual API key to version control. The placeholder `${OPENAI_API_KEY:your-openai-api-key-here}` is designed to use environment variables.

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/alokpandey/ise-policy-engine.git
   cd ise-policy-engine
   ```

2. **Set up OpenAI API Key**
   ```bash
   export OPENAI_API_KEY=your-actual-openai-api-key-here
   ```

3. **Build the application**
   ```bash
   mvn clean compile
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

5. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Start the Admin Portal (Optional)**
   ```bash
   cd admin-portal
   npm install
   npm start
   ```

The application will start on:
- **Backend API**: `http://localhost:8080/api/v1`
- **Admin Portal**: `http://localhost:3000` (if running)
- **Health Check**: `http://localhost:8080/actuator/health`

### Default Credentials

- **Username**: `admin`
- **Password**: `admin123`
- **Role**: ADMIN

Alternative user:
- **Username**: `user`
- **Password**: `user123`
- **Role**: USER

## 📚 API Documentation

### Core Endpoints

#### Policy Management
- `POST /api/v1/policies` - Create a new policy
- `PUT /api/v1/policies/{policyId}` - Update an existing policy
- `POST /api/v1/policies/{policyId}/activate` - Activate a policy
- `POST /api/v1/policies/{policyId}/deactivate` - Deactivate a policy

#### Policy Recommendations
- `POST /api/v1/policies/recommendations` - Get AI-driven policy recommendations
- `POST /api/v1/policies/execute` - Execute policy recommendations

#### Monitoring
- `GET /api/v1/policies/health` - Health check endpoint
- `GET /api/v1/policies/{policyId}/executions` - Get policy execution history
- `GET /api/v1/policies/sessions/{sessionId}/executions` - Get session execution history

#### Actuator Endpoints
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/info` - Application information

### Sample API Calls

#### Create a Policy
```bash
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "name": "High Risk User Policy",
    "description": "Policy for high-risk user sessions",
    "type": "AUTHORIZATION",
    "priority": 1,
    "conditions": "{\"riskScore\": {\"gt\": 0.8}}",
    "actions": "{\"action\": \"QUARANTINE\"}"
  }'
```

#### Get Policy Recommendations
```bash
curl -X POST http://localhost:8080/api/v1/policies/recommendations \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "sessionId": "session-123",
    "userName": "john.doe",
    "deviceMac": "00:11:22:33:44:55",
    "ipAddress": "192.168.1.100",
    "riskScore": 0.75,
    "location": "guest_network"
  }'
```

## 🔧 Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api/v1

# OpenAI Configuration
openai:
  api:
    key: ${OPENAI_API_KEY:your-openai-api-key-here}
    timeout: 60

# ISE Integration Configuration
ise:
  mock:
    enabled: true
    base-url: http://localhost:9090
  client:
    timeout: 30000
    retry-attempts: 3

# AI/ML Configuration
ai:
  risk-threshold: 0.7
  anomaly-threshold: 0.8
  learning-rate: 0.01
  recommendation:
    confidence-threshold: 0.8

# Policy Configuration
policy:
  simulation:
    enabled: true
  lifecycle:
    auto-approval-threshold: 0.9
    manual-approval-required: false

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

# Security Configuration
security:
  basic:
    enabled: true
  users:
    admin:
      password: admin123
      roles: ADMIN
    user:
      password: user123
      roles: USER
```

## 🧪 Testing

The project includes comprehensive tests:

- **Unit Tests**: Test individual components
- **Integration Tests**: Test component interactions
- **Mock Tests**: Test with mocked external dependencies
- **AI Integration Tests**: Test OpenAI integration with mock responses

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=PolicyManagementApplicationTest
```

Run tests with coverage:
```bash
mvn test jacoco:report
```

Run integration tests only:
```bash
mvn test -Dtest="*IntegrationTest"
```

### Test Configuration

For testing, you don't need a real OpenAI API key. The tests use mocked responses. However, if you want to test with real AI integration, set:

```bash
export OPENAI_API_KEY=your-test-api-key
mvn test -Dspring.profiles.active=integration
```

## 🔍 Monitoring and Observability

The application includes:

- **Health Checks**: Spring Boot Actuator health endpoints
- **Metrics**: Application and business metrics
- **Logging**: Structured logging with configurable levels
- **Audit Trail**: Complete audit logging of policy actions

## 🛡️ Security

- **Authentication**: HTTP Basic Authentication
- **Authorization**: Role-based access control (ADMIN, USER)
- **Input Validation**: Request validation using Bean Validation
- **Security Headers**: Standard security headers configured
- **Audit Logging**: All security-relevant actions are logged

## 🚀 Deployment

### Docker Deployment (Future Enhancement)
```dockerfile
FROM openjdk:17-jre-slim
COPY target/intelligent-policy-management-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment (Future Enhancement)
- Helm charts for easy deployment
- ConfigMaps for configuration management
- Secrets for sensitive data
- Health checks and readiness probes

## 🔧 Troubleshooting

### Common Issues

**1. OpenAI API Key Issues**
```
Error: OpenAI API key not configured
```
- Ensure you've set the `OPENAI_API_KEY` environment variable
- Verify your API key is valid and has sufficient credits
- Check that the key hasn't expired

**2. Port Already in Use**
```
Error: Port 8080 is already in use
```
- Change the port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```
- Or set via environment: `export SERVER_PORT=8081`

**3. Database Connection Issues**
```
Error: Could not create connection to database server
```
- For H2 (default): No action needed, uses in-memory database
- For PostgreSQL: Ensure PostgreSQL is running and credentials are correct

**4. Admin Portal Not Loading**
- Ensure Node.js 16+ is installed
- Run `npm install` in the `admin-portal` directory
- Check if port 3000 is available

**5. Maven Build Issues**
```
Error: Could not resolve dependencies
```
- Ensure you have Java 17+ installed
- Run `mvn clean install -U` to force update dependencies
- Check your internet connection for dependency downloads

### Debug Mode

Enable debug logging:
```bash
mvn spring-boot:run -Dlogging.level.com.cisco.ise.ai=DEBUG
```

Or set in `application.yml`:
```yaml
logging:
  level:
    com.cisco.ise.ai: DEBUG
    org.springframework.security: DEBUG
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding standards and best practices
- Write comprehensive tests for new features
- Update documentation for any API changes
- Ensure all tests pass before submitting PR
- Use meaningful commit messages

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation and API examples

## 🗺️ Roadmap

- [ ] Real ISE integration (replace mock)
- [ ] Advanced ML models for risk assessment
- [ ] Policy simulation dashboard
- [ ] Real-time threat intelligence integration
- [ ] Kubernetes deployment manifests
- [ ] Performance optimization
- [ ] Enhanced monitoring and alerting
