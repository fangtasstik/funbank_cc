#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "🏦 Funbank Service Verification Script"
echo "======================================="

# Function to check if a service is running on a port
check_service() {
    local service_name=$1
    local port=$2
    local endpoint=${3:-"/actuator/health"}
    
    echo -n "Checking $service_name (port $port)... "
    
    # Check if port is open
    if ! nc -z localhost $port 2>/dev/null; then
        echo -e "${RED}❌ Port $port not accessible${NC}"
        return 1
    fi
    
    # Check HTTP endpoint
    response=$(curl -s -w "%{http_code}" http://localhost:$port$endpoint 2>/dev/null)
    http_code="${response: -3}"
    
    if [[ "$http_code" == "200" ]]; then
        echo -e "${GREEN}✅ Running and healthy${NC}"
        return 0
    elif [[ "$http_code" == "000" ]]; then
        echo -e "${YELLOW}⚠️  Port open but HTTP not responding${NC}"
        return 1
    else
        echo -e "${YELLOW}⚠️  HTTP $http_code${NC}"
        return 1
    fi
}

# Function to check Docker containers
check_docker_services() {
    echo -e "\n${BLUE}📦 Docker Infrastructure Services:${NC}"
    
    local services=(
        "funbank-redis:6380"
        "funbank-mysql:3307" 
        "funbank-mongodb:27017"
        "funbank-adminer:8080"
        "funbank-redis-commander:8082"
        "jaeger:16686"
    )
    
    for service_port in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        if docker ps --format '{{.Names}}' | grep -q "$service"; then
            if [[ "$service" == "jaeger" ]]; then
                check_service "$service" "$port" "/"
            elif [[ "$service" == "funbank-mysql" ]] || [[ "$service" == "funbank-mongodb" ]] || [[ "$service" == "funbank-redis" ]]; then
                echo -n "Checking $service (port $port)... "
                if nc -z localhost $port 2>/dev/null; then
                    echo -e "${GREEN}✅ Running${NC}"
                else
                    echo -e "${RED}❌ Not accessible${NC}"
                fi
            else
                check_service "$service" "$port" "/"
            fi
        else
            echo -e "Checking $service... ${RED}❌ Container not running${NC}"
        fi
    done
}

# Function to check Java microservices
check_java_services() {
    echo -e "\n${BLUE}☕ Java Microservices:${NC}"
    
    check_service "Config Server" "8888"
    check_service "Service Registry (Eureka)" "8761"
    check_service "API Gateway" "9090"
}

# Function to test service discovery
test_service_discovery() {
    echo -e "\n${BLUE}🔍 Service Discovery Test:${NC}"
    
    if check_service "Eureka" "8761" "/"; then
        echo "Checking registered services..."
        
        # Try to get registered services
        apps=$(curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" 2>/dev/null)
        if [[ $? -eq 0 ]] && [[ -n "$apps" ]]; then
            echo -e "${GREEN}✅ Eureka responding${NC}"
            
            # Count registered services
            if command -v jq >/dev/null 2>&1; then
                count=$(echo "$apps" | jq '.applications.application | length' 2>/dev/null || echo "0")
                echo "📊 Registered services: $count"
            fi
        else
            echo -e "${YELLOW}⚠️  Eureka accessible but no service data${NC}"
        fi
    fi
}

# Function to test configuration server
test_config_server() {
    echo -e "\n${BLUE}⚙️  Configuration Server Test:${NC}"
    
    if check_service "Config Server" "8888" "/actuator/health"; then
        echo "Testing config retrieval..."
        
        # Test config for gateway
        config=$(curl -s http://localhost:8888/config/funbank-api-gateway/dev 2>/dev/null)
        if [[ $? -eq 0 ]] && [[ -n "$config" ]]; then
            echo -e "${GREEN}✅ Config server serving configurations${NC}"
        else
            echo -e "${YELLOW}⚠️  Config server healthy but not serving configs${NC}"
        fi
    fi
}

# Function to test API Gateway
test_api_gateway() {
    echo -e "\n${BLUE}🚪 API Gateway Test:${NC}"
    
    if check_service "API Gateway" "9090" "/actuator/health"; then
        echo "Testing gateway endpoints..."
        
        # Test actuator endpoints
        endpoints=("/actuator/info" "/actuator/metrics" "/actuator/gateway/routes")
        
        for endpoint in "${endpoints[@]}"; do
            response=$(curl -s -w "%{http_code}" http://localhost:9090$endpoint 2>/dev/null)
            http_code="${response: -3}"
            
            if [[ "$http_code" == "200" ]]; then
                echo -e "  $endpoint: ${GREEN}✅${NC}"
            else
                echo -e "  $endpoint: ${RED}❌ ($http_code)${NC}"
            fi
        done
    fi
}

# Function to show next steps
show_next_steps() {
    echo -e "\n${BLUE}🚀 Next Steps:${NC}"
    echo "1. If services aren't running, start them with:"
    echo "   ./scripts/start-all-services.sh"
    echo ""
    echo "2. Access service dashboards:"
    echo "   • Eureka Dashboard: http://localhost:8761"
    echo "   • Jaeger Tracing: http://localhost:16686"
    echo "   • Redis Commander: http://localhost:8082"
    echo "   • MySQL Adminer: http://localhost:8080"
    echo ""
    echo "3. Test API Gateway:"
    echo "   curl http://localhost:9090/actuator/health"
    echo ""
    echo "4. Run integration tests:"
    echo "   ./scripts/comprehensive-test.sh"
}

# Main execution
echo -e "${YELLOW}Starting comprehensive service verification...${NC}\n"

check_docker_services
check_java_services
test_service_discovery
test_config_server  
test_api_gateway
show_next_steps

echo -e "\n${BLUE}📋 Verification complete!${NC}"