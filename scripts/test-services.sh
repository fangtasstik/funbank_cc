#!/bin/bash

echo "🧪 Funbank Services Integration Test"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test function
test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $name... "
    
    if command -v curl &> /dev/null; then
        status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
        if [ "$status" = "$expected_status" ]; then
            echo -e "${GREEN}✅ PASS${NC} (HTTP $status)"
        else
            echo -e "${RED}❌ FAIL${NC} (HTTP $status, expected $expected_status)"
        fi
    else
        echo -e "${YELLOW}⚠️ SKIP${NC} (curl not available)"
    fi
}

echo ""
echo "1️⃣ Testing Config Server (Port 8888)"
echo "-----------------------------------"
test_endpoint "Config Server Health" "http://localhost:8888/config/actuator/health"
test_endpoint "Gateway Config (Default)" "http://funbank-config:funbank-config-2024@localhost:8888/config/funbank-api-gateway/default"
test_endpoint "Gateway Config (Prod)" "http://funbank-config:funbank-config-2024@localhost:8888/config/funbank-api-gateway/prod"

echo ""
echo "2️⃣ Testing Service Registry (Port 8761)"
echo "---------------------------------------"
test_endpoint "Eureka Server" "http://localhost:8761/"
test_endpoint "Eureka Apps API" "http://localhost:8761/eureka/apps"

echo ""
echo "3️⃣ Testing API Gateway (Port 9090)"
echo "----------------------------------"
test_endpoint "Gateway Health" "http://localhost:9090/actuator/health"
test_endpoint "Gateway Info" "http://localhost:9090/actuator/info"

echo ""
echo "4️⃣ Testing Gateway Features"
echo "---------------------------"

# Test CORS
echo -n "Testing CORS... "
cors_result=$(curl -s -H "Origin: http://localhost:3000" \
                   -H "Access-Control-Request-Method: GET" \
                   -X OPTIONS \
                   http://localhost:9090/actuator/health 2>/dev/null | grep -i "access-control" | wc -l)
if [ "$cors_result" -gt 0 ]; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${RED}❌ FAIL${NC}"
fi

# Test if Gateway is registered in Eureka
echo -n "Testing Gateway Registration... "
if curl -s http://localhost:8761/eureka/apps 2>/dev/null | grep -q "FUNBANK-API-GATEWAY"; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${RED}❌ FAIL${NC} (Not registered in Eureka)"
fi

echo ""
echo "🎯 Test Complete!"
echo ""
echo "📋 Manual Tests:"
echo "- Open http://localhost:8761 for Eureka Dashboard"
echo "- Check Gateway routes: curl http://localhost:9090/actuator/gateway/routes"
echo "- Test rate limiting with multiple quick requests"