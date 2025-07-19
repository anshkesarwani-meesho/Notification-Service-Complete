# Notification Service

A robust SMS notification service built with Spring Boot, Apache Kafka, Redis, MySQL, and Elasticsearch. This service provides reliable SMS delivery with message persistence, search capabilities, and real-time processing.

## 🚀 Features

- **SMS Sending**: Send SMS messages via REST API
- **Kafka Integration**: Asynchronous message processing with Apache Kafka
- **Message Persistence**: Store SMS requests in MySQL database
- **Search & Analytics**: Full-text search using Elasticsearch
- **Caching**: Redis-based caching for improved performance
- **Rate Limiting**: Configurable rate limiting for SMS sending
- **Blacklist Management**: Block specific phone numbers
- **Health Monitoring**: Spring Boot Actuator for monitoring
- **API Documentation**: OpenAPI/Swagger documentation
- **Distributed Tracing**: Zipkin integration for request tracing

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.5.0**
- **Apache Kafka** - Message queuing
- **MySQL** - Primary database
- **Redis** - Caching layer
- **Elasticsearch** - Search and analytics
- **Flyway** - Database migrations
- **Log4j2** - Logging framework
- **Lombok** - Code generation
- **OpenAPI** - API documentation

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Apache Kafka 3.0+
- Elasticsearch 8.0+

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd notification-service
```

### 2. Database Setup

Create a MySQL database and update the configuration in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notification_db
    username: your_username
    password: your_password
```

### 3. Start Dependencies

#### Start MySQL

```bash
# macOS (using Homebrew)
brew services start mysql

# Or start manually
mysql.server start
```

#### Start Redis

```bash
# macOS (using Homebrew)
brew services start redis

# Or start manually
redis-server
```

#### Start Kafka

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

#### Start Elasticsearch

```bash
# macOS (using Homebrew)
brew services start elasticsearch

# Or start manually
elasticsearch
```

### 4. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/notification-service-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL

```
http://localhost:8080/v1
```

### Endpoints

#### Send SMS

```http
POST /sms/send
Content-Type: application/json

{
  "phoneNumber": "1234567890",
  "message": "Hello from Notification Service!"
}
```

#### Get SMS Request

```http
GET /sms/{requestId}
```

#### Search SMS Requests

```http
GET /sms/search?phoneNumber=1234567890&startTime=2024-01-01T00:00:00Z&endTime=2024-12-31T23:59:59Z
```

**Query Parameters:**
- `phoneNumber` (optional): 10-digit phone number
- `startTime` (optional): Start time in ISO format
- `endTime` (optional): End time in ISO format
- `text` (optional): Search text in message content
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

#### Blacklist Management

**Add to Blacklist:**
```http
POST /blacklist
Content-Type: application/json

{
  "phoneNumbers": ["1234567890", "9876543210"],
  "reason": "Spam"
}
```

**Remove from Blacklist:**
```http
DELETE /blacklist
Content-Type: application/json

{
  "phoneNumbers": ["1234567890", "9876543210"]
}
```

**Get All Blacklisted Numbers:**
```http
GET /blacklist
```

### Health Check

```http
GET /actuator/health
```

## 🔧 Configuration

### Environment Variables

| Variable                    | Description          | Default                                              |
| --------------------------- | -------------------- | ---------------------------------------------------- |
| `MYSQL_USERNAME`          | MySQL username       | `root`                                             |
| `MYSQL_PASSWORD`          | MySQL password       | `anshmeesho@123`                                   |
| `REDIS_HOST`              | Redis host           | `localhost`                                        |
| `REDIS_PORT`              | Redis port           | `6379`                                             |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers        | `localhost:9092`                                   |
| `ELASTICSEARCH_HOST`      | Elasticsearch host   | `localhost`                                        |
| `ELASTICSEARCH_PORT`      | Elasticsearch port   | `9200`                                             |
| `SMS_API_KEY`             | SMS provider API key | -                                                    |
| `SMS_PROVIDER_URL`        | SMS provider URL     | `https://api.imiconnect.in/resources/v1/messaging` |

### Application Properties

Key configuration options in `application.yml`:

- **Rate Limiting**: Configure SMS rate limits per minute/hour
- **Retry Settings**: Set maximum retries and delay for failed SMS
- **Cache TTL**: Configure Redis cache expiration
- **Logging Levels**: Adjust log verbosity

## 🏗️ Project Structure

```
src/main/java/com/notification/
├── config/                 # Configuration classes
├── controller/             # REST controllers
├── dto/                   # Data Transfer Objects
├── exception/             # Custom exceptions
├── kafka/                 # Kafka producers/consumers
├── model/                 # Entity classes
├── repository/            # Data access layer
├── service/               # Business logic
└── utils/                 # Utility classes
```

## 🧪 Testing

Run the test suite:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SmsServiceTest

# Run with coverage
mvn jacoco:report
```

## 📊 Monitoring

### Health Checks

- Application health: `GET /actuator/health`
- Database health: `GET /actuator/health/db`
- Redis health: `GET /actuator/health/redis`
- Kafka health: `GET /actuator/health/kafka`

### Metrics

- Application metrics: `GET /actuator/metrics`
- Custom metrics available via Micrometer

### Logs

- Application logs: `logs/application.log`
- Service-specific logs: `logs/notification-service.log`

## 🔒 Security

- Input validation using Bean Validation
- Rate limiting to prevent abuse
- Blacklist functionality for spam prevention
- Secure configuration management

## 🚀 Deployment

### Docker

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes

Example deployment configuration available in `k8s/` directory.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the documentation in `/docs` directory

## 🔄 Changelog

### v0.0.1-SNAPSHOT

- Initial release
- SMS sending functionality
- Kafka integration
- Elasticsearch search
- Redis caching
- Blacklist management
- Health monitoring
- API documentation

---

**Note**: This is a development version. For production deployment, ensure proper security configurations and environment-specific settings.
