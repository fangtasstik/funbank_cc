#!/bin/bash

echo "🚀 Starting Funbank Development Services"
echo "========================================"

# Check if services are already running
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "⚠️  $service already running on port $port"
        return 1
    else
        echo "✅ Port $port available for $service"
        return 0
    fi
}

echo ""
echo "🔍 Checking service ports..."
check_port 8888 "Config Server"
check_port 8761 "Service Registry" 
check_port 9090 "API Gateway"

echo ""
echo "📋 To start all services manually:"
echo ""
echo "1️⃣ Config Server (Terminal 1):"
echo "   cd funbank-config-server"
echo "   mvn spring-boot:run"
echo ""
echo "2️⃣ Service Registry (Terminal 2):"  
echo "   cd funbank-service-registry"
echo "   mvn spring-boot:run"
echo ""
echo "3️⃣ API Gateway (Terminal 3):"
echo "   cd funbank_cc"
echo "   SPRING_PROFILES_ACTIVE=dev java -jar funbank-api-gateway/target/funbank-api-gateway-1.0.0.jar"
echo ""
echo "⏱️ Wait 30-60 seconds between starting each service for proper initialization"
echo ""
echo "🧪 After all services are running:"
echo "   ./scripts/test-services.sh"
echo ""
echo "🌐 Service URLs:"
echo "   Config Server:    http://localhost:8888/config"
echo "   Service Registry: http://localhost:8761"
echo "   API Gateway:      http://localhost:9090"
echo "   Health Check:     http://localhost:9090/actuator/health"