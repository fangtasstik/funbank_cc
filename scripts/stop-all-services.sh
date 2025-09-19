#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "🛑 Funbank Services Stop Script"
echo "==============================="

# Function to stop service by port
stop_service_by_port() {
    local service_name=$1
    local port=$2
    
    echo -n "Stopping $service_name (port $port)... "
    
    # Find process using the port
    local pid=$(lsof -ti:$port 2>/dev/null)
    
    if [[ -n "$pid" ]]; then
        # Try graceful shutdown first
        kill $pid 2>/dev/null
        
        # Wait up to 10 seconds for graceful shutdown
        local attempts=0
        while [ $attempts -lt 10 ]; do
            if ! kill -0 $pid 2>/dev/null; then
                echo -e "${GREEN}✅ Stopped${NC}"
                return 0
            fi
            sleep 1
            attempts=$((attempts + 1))
        done
        
        # Force kill if still running
        echo -n "Force stopping... "
        kill -9 $pid 2>/dev/null
        sleep 1
        
        if ! kill -0 $pid 2>/dev/null; then
            echo -e "${GREEN}✅ Force stopped${NC}"
        else
            echo -e "${RED}❌ Failed to stop${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Not running${NC}"
    fi
}

# Stop all Java services
echo -e "${BLUE}Stopping Java microservices...${NC}"

stop_service_by_port "API Gateway" "9090"
stop_service_by_port "Service Registry (Eureka)" "8761" 
stop_service_by_port "Config Server" "8888"

# Clean up PID files
echo -e "\n${BLUE}Cleaning up PID files...${NC}"
rm -f /home/antaridge/projects/funbank_cc/logs/*.pid

# Optional: Stop Docker services
echo -e "\n${YELLOW}Docker services are still running.${NC}"
echo "To stop Docker infrastructure:"
echo "  docker-compose down"

echo -e "\n${GREEN}✅ Java services stopped${NC}"