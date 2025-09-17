# Funbank Scripts Directory

This directory contains all the shell scripts for building, testing, and managing the Funbank banking system.

## 📁 Script Categories

### 🔨 Build & Development
- **`build-all.sh`** - Builds all Funbank microservices using Maven
- **`start-dev-services.sh`** - Starts essential development services (databases, config server, etc.)

### 🔍 Testing Scripts
- **`comprehensive-test.sh`** - Runs comprehensive tests across all services
- **`test-services.sh`** - Basic service health testing
- **`test-suncorp-stack.sh`** - Tests Suncorp-aligned testing stack
- **`test-otel.sh`** - Tests OpenTelemetry tracing configuration

### 📊 Observability & Monitoring
- **`verify-observability-stack.sh`** - Verifies all observability services are healthy
- **`generate-test-traffic.sh`** - Generates realistic banking API traffic for testing
- **`start-jaeger.sh`** - Starts Jaeger tracing service standalone
- **`start-gateway-with-otel.sh`** - Starts API Gateway with OpenTelemetry instrumentation
- **`verify-otel.sh`** - Verifies OpenTelemetry configuration and tracing

## 🚀 Quick Start

### Test the Complete Stack
```bash
# Start everything
docker-compose up -d

# Verify observability stack
./scripts/verify-observability-stack.sh

# Generate test data
./scripts/generate-test-traffic.sh
```

### Development Workflow
```bash
# Build all services
./scripts/build-all.sh

# Start development services
./scripts/start-dev-services.sh

# Run comprehensive tests
./scripts/comprehensive-test.sh
```

### Observability Testing
```bash
# Test OpenTelemetry setup
./scripts/test-otel.sh

# Start gateway with tracing
./scripts/start-gateway-with-otel.sh

# Verify tracing is working
./scripts/verify-otel.sh
```

## 📋 Script Details

### Dependencies
Most scripts require:
- Docker & Docker Compose
- Maven 3.6+
- Java 17+
- curl & jq (for API testing)

### Permissions
All scripts are executable. If you need to make them executable:
```bash
chmod +x scripts/*.sh
```

### Logging
Scripts output to console with color-coded status messages:
- 🟢 Green: Success
- 🟡 Yellow: Warning
- 🔴 Red: Error

### Environment Variables
Some scripts respect environment variables:
- `JAVA_HOME` - Java installation path
- `MAVEN_HOME` - Maven installation path
- `DOCKER_HOST` - Docker daemon host

## 🔧 Customization

To modify script behavior:
1. Copy the script to a new name
2. Modify the configuration variables at the top
3. Update any hardcoded URLs or ports as needed

## 📞 Support

For script issues:
1. Check the script logs/output
2. Verify dependencies are installed
3. Ensure Docker services are running
4. Check the main project documentation