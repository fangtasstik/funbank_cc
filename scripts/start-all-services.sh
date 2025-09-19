#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "🏦 Funbank Services Startup Script"
echo "=================================="

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local profile=${4:-dev}
    
    echo -e "\n${BLUE}Starting $service_name...${NC}"
    
    cd "/home/antaridge/projects/funbank_cc/$service_dir"
    
    # Check if service is already running
    if nc -z localhost $port 2>/dev/null; then
        echo -e "${YELLOW}⚠️  Service already running on port $port${NC}"
        return 0
    fi
    
    # Start the service in background
    echo "Starting on port $port with profile: $profile"
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=$profile > "../logs/${service_name}.log" 2>&1 &
    local pid=$!
    
    echo "PID: $pid"
    echo $pid > "../logs/${service_name}.pid"
    
    # Wait a bit and check if it started
    echo "Waiting for service to start..."
    sleep 10
    
    # Check if service is responding
    local attempts=0
    local max_attempts=12  # 60 seconds total
    
    while [ $attempts -lt $max_attempts ]; do
        if nc -z localhost $port 2>/dev/null; then
            echo -e "${GREEN}✅ $service_name started successfully${NC}"
            return 0
        fi
        
        attempts=$((attempts + 1))
        echo -n "."
        sleep 5
    done
    
    echo -e "\n${RED}❌ $service_name failed to start on port $port${NC}"
    echo "Check log: ../logs/${service_name}.log"
    return 1
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    
    # Check if Docker services are running
    local required_containers=("funbank-redis" "funbank-mysql" "funbank-mongodb")
    
    for container in "${required_containers[@]}"; do
        if ! docker ps --format '{{.Names}}' | grep -q "$container"; then
            echo -e "${RED}❌ Docker container $container is not running${NC}"
            echo "Please start Docker infrastructure first:"
            echo "  docker-compose up -d"
            return 1
        fi
    done
    
    echo -e "${GREEN}✅ Docker infrastructure is running${NC}"
    
    # Check if logs directory exists
    mkdir -p "/home/antaridge/projects/funbank_cc/logs"
    
    return 0
}

# Function to stop existing services
stop_existing_services() {
    echo -e "\n${BLUE}🛑 Stopping any existing Java services...${NC}"
    
    # Kill processes using the service ports
    local ports=(8888 8761 9090)
    
    for port in "${ports[@]}"; do
        local pid=$(lsof -ti:$port 2>/dev/null)
        if [[ -n "$pid" ]]; then
            echo "Stopping process on port $port (PID: $pid)"
            kill $pid 2>/dev/null
            sleep 2
        fi
    done
    
    # Clean up old PID files
    rm -f /home/antaridge/projects/funbank_cc/logs/*.pid
}

# Main startup sequence
main() {
    # Check prerequisites
    if ! check_prerequisites; then
        exit 1
    fi
    
    # Stop existing services
    stop_existing_services
    
    echo -e "\n${YELLOW}📦 Starting services in correct order...${NC}"
    
    # Start services in dependency order
    
    # 1. Config Server (needed by other services)
    if ! start_service "Config Server" "funbank-config-server" "8888" "dev"; then
        echo -e "${RED}❌ Failed to start Config Server${NC}"
        exit 1
    fi
    
    # 2. Service Registry (Eureka)
    if ! start_service "Service Registry" "funbank-service-registry" "8761" "dev"; then
        echo -e "${RED}❌ Failed to start Service Registry${NC}"
        exit 1
    fi
    
    # 3. API Gateway
    if ! start_service "API Gateway" "funbank-api-gateway" "9090" "dev"; then
        echo -e "${RED}❌ Failed to start API Gateway${NC}"
        exit 1
    fi
    
    echo -e "\n${GREEN}🎉 All services started successfully!${NC}"
    echo -e "\n${BLUE}📋 Service URLs:${NC}"
    echo "• Config Server: http://localhost:8888/actuator/health"
    echo "• Eureka Dashboard: http://localhost:8761"
    echo "• API Gateway: http://localhost:9090/actuator/health"
    
    echo -e "\n${BLUE}📝 Logs location:${NC}"
    echo "• Config Server: logs/Config Server.log"
    echo "• Service Registry: logs/Service Registry.log" 
    echo "• API Gateway: logs/API Gateway.log"
    
    echo -e "\n${BLUE}🔧 Management commands:${NC}"
    echo "• Stop services: ./scripts/stop-all-services.sh"
    echo "• Verify services: ./scripts/verify-all-services.sh"
    echo "• View logs: tail -f logs/[service-name].log"
}

# Run main function
main "$@"