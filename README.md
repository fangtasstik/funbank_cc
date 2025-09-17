# 🏦 Funbank - Enterprise Banking Microservices System

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]() [![Coverage](https://img.shields.io/badge/coverage-80%25-green)]() [![License](https://img.shields.io/badge/license-MIT-blue)]()

A comprehensive event-driven banking system built with Spring Cloud microservices, featuring enterprise-grade observability, testing, and security.

## 🚀 Quick Start

```bash
# 1. Start the complete stack
docker-compose up -d

# 2. Verify everything is running
./scripts/verify-observability-stack.sh

# 3. Generate test data
./scripts/generate-test-traffic.sh

# 4. Access monitoring dashboards
open http://localhost:3001  # Grafana (admin/funbank_admin_2024)
```

## 📁 Project Structure

```
funbank_cc/
├── 📊 docker/                    # Docker configurations
│   ├── grafana/                  # Grafana dashboards & config
│   ├── prometheus/               # Prometheus configuration
│   ├── logstash/                 # ELK stack configuration
│   └── ...                       # Database & service configs
├── 📖 docs/                      # Project documentation
│   ├── project_plan.md           # Detailed project roadmap
│   ├── funbank_architecture.md   # System architecture
│   └── ...                       # Technical guides
├── 🚀 scripts/                   # All shell scripts
│   ├── build-all.sh              # Build all services
│   ├── verify-observability-stack.sh  # Test monitoring
│   ├── generate-test-traffic.sh  # Create test data
│   └── ...                       # Development & testing scripts
├── 🏗️ funbank-api-gateway/       # API Gateway service
├── 🔧 funbank-common/            # Shared utilities
├── ⚙️ funbank-config-server/     # Configuration management
├── 🗂️ funbank-service-registry/  # Service discovery
└── 🧪 testing/                   # Test fixtures & stubs
```

## 🎯 Core Features

### 🏦 Banking Services
- **API Gateway** - Single entry point with security & routing
- **Service Discovery** - Eureka-based service registration
- **Configuration Management** - Centralized config with Spring Cloud Config
- **Event-Driven Architecture** - CQRS pattern with event sourcing

### 📊 Enterprise Observability
- **Metrics** - Prometheus + 4 custom Grafana dashboards
- **Logging** - ELK Stack with structured JSON logging
- **Tracing** - OpenTelemetry + Jaeger distributed tracing
- **Monitoring** - Banking-specific KPIs and security metrics

### 🧪 Comprehensive Testing
- **Unit Testing** - JUnit 5 + Mockito + AssertJ
- **Integration Testing** - TestContainers + REST Assured
- **Contract Testing** - Pact for consumer-driven contracts
- **API Testing** - WireMock for external service mocking
- **Code Quality** - JaCoCo with 80% coverage threshold

## 🌐 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| 🎯 **Grafana** | http://localhost:3001 | admin / funbank_admin_2024 |
| 📊 **Prometheus** | http://localhost:9090 | None |
| 📋 **Kibana** | http://localhost:5601 | None |
| 🔍 **Jaeger** | http://localhost:16686 | None |
| 🚪 **API Gateway** | http://localhost:8080 | None |
| 🗂️ **Eureka** | http://localhost:8761 | None |
| ⚙️ **Config Server** | http://localhost:8888 | None |

## 🛠️ Development Workflow

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Git

### Setup & Build
```bash
# Clone and build
git clone <repository>
cd funbank_cc
./scripts/build-all.sh
```

### Development Mode
```bash
# Start infrastructure only
./scripts/start-dev-services.sh

# Run specific service locally
cd funbank-api-gateway
mvn spring-boot:run
```

### Testing
```bash
# Run all tests
./scripts/comprehensive-test.sh

# Test specific component
./scripts/test-otel.sh
./scripts/test-suncorp-stack.sh
```

## 📊 Monitoring Dashboards

### 🎯 Grafana Dashboards (http://localhost:3001)
1. **🚪 API Gateway Dashboard** - Request rates, response times, error tracking
2. **💰 Banking Operations** - Transaction volumes, authentication metrics
3. **🏗️ Infrastructure** - Service health, JVM metrics, database connections
4. **🔐 Security Monitoring** - Failed logins, access patterns, threats

### 📈 Key Metrics
- Transaction throughput & latency
- Authentication success/failure rates
- System availability & error rates
- Security events & anomalies
- Resource utilization (CPU, memory, DB connections)

## 🔒 Security Features

- **JWT Authentication** - Stateless token-based auth
- **Rate Limiting** - API throttling & DDoS protection
- **CORS Configuration** - Cross-origin security
- **Audit Logging** - Complete transaction trails
- **Security Monitoring** - Real-time threat detection

## 🏗️ Architecture

**Microservices Pattern** with:
- Event-driven communication
- CQRS (Command Query Responsibility Segregation)
- Event sourcing for audit trails
- Circuit breaker pattern for resilience
- Distributed tracing for observability

## 📚 Documentation

- 📋 **[Project Plan](docs/project_plan.md)** - Detailed roadmap & progress
- 🏗️ **[Architecture Guide](docs/funbank_architecture.md)** - System design
- 🔧 **[Build Guide](docs/build_and_development_guide.md)** - Development setup
- 🧪 **[Testing Guide](docs/observability-testing-guide.md)** - Observability testing
- 📜 **[Scripts Reference](scripts/README.md)** - All available scripts

## 🚦 Project Status

**Current Phase**: Phase 2 Complete ✅ (100%)

- ✅ **Phase 1**: Infrastructure (Databases, Docker, Admin tools)
- ✅ **Phase 2**: Microservices Core (API Gateway, Config, Discovery, Observability)
- 🔄 **Phase 2.5**: Comprehensive Testing (In Progress)
- 📋 **Phase 3**: Production Ready (Kubernetes, CI/CD)

## 🤝 Contributing

1. Follow the coding standards and project structure rules in `rules/`
2. Ensure 80%+ test coverage
3. Update documentation for new features
4. Test with the observability stack

## 📞 Support

- 📖 Check the documentation in `docs/`
- 🔧 Run `./scripts/verify-observability-stack.sh` for health checks
- 🐛 Check logs with `docker-compose logs [service-name]`

---

**Built with Spring Boot 3.1, Spring Cloud 2022, OpenTelemetry, and enterprise-grade observability tools** 🚀