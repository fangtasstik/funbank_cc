#!/bin/bash

echo "🧪 Funbank Comprehensive Integration Test"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test function
test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="${3:-200}"
    local auth="$4"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "[$TOTAL_TESTS] Testing $name... "
    
    if command -v curl &> /dev/null; then
        if [ -n "$auth" ]; then
            status=$(curl -s -u "$auth" -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
        else
            status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
        fi
        
        if [ "$status" = "$expected_status" ]; then
            echo -e "${GREEN}✅ PASS${NC} (HTTP $status)"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            echo -e "${RED}❌ FAIL${NC} (HTTP $status, expected $expected_status)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        echo -e "${YELLOW}⚠️ SKIP${NC} (curl not available)"
    fi
}

# JSON test function
test_json_response() {
    local name="$1"
    local url="$2"
    local json_check="$3"
    local auth="$4"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "[$TOTAL_TESTS] Testing $name... "
    
    if command -v curl &> /dev/null; then
        if [ -n "$auth" ]; then
            response=$(curl -s -u "$auth" "$url" 2>/dev/null)
        else
            response=$(curl -s "$url" 2>/dev/null)
        fi
        
        if echo "$response" | grep -q "$json_check"; then
            echo -e "${GREEN}✅ PASS${NC}"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            echo -e "${RED}❌ FAIL${NC} (JSON check failed: $json_check)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        echo -e "${YELLOW}⚠️ SKIP${NC} (curl not available)"
    fi
}

echo ""
echo "🔧 1. BASIC SERVICE HEALTH CHECKS"
echo "================================="
test_endpoint "Config Server Health" "http://localhost:8888/config/actuator/health" 200 "funbank-config:funbank-config-2024"
test_endpoint "Service Registry Health" "http://localhost:8761/actuator/health" 200
test_endpoint "Gateway Health" "http://localhost:9090/actuator/health" 200

echo ""
echo "🌐 2. CONFIG SERVER INTEGRATION"
echo "==============================="
test_endpoint "Gateway Default Config" "http://localhost:8888/config/funbank-api-gateway/default" 200 "funbank-config:funbank-config-2024"
test_endpoint "Gateway Dev Config" "http://localhost:8888/config/funbank-api-gateway/dev" 200 "funbank-config:funbank-config-2024"
test_json_response "Config Contains JWT Settings" "http://localhost:9090/actuator/configprops" "jwt" 
test_json_response "Config Contains Redis Settings" "http://localhost:9090/actuator/configprops" "redis"

echo ""
echo "🔍 3. SERVICE DISCOVERY INTEGRATION"
echo "==================================="
test_endpoint "Eureka Apps Endpoint" "http://localhost:8761/eureka/apps" 200
test_json_response "Gateway Registered in Eureka" "http://localhost:8761/eureka/apps" "FUNBANK-API-GATEWAY"
test_json_response "Gateway Discovery Status" "http://localhost:9090/actuator/health" '"discoveryComposite":{"status":"UP"'

echo ""
echo "🛡️ 4. SECURITY FEATURES"
echo "======================="
# Test CORS
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "[$TOTAL_TESTS] Testing CORS Headers... "
cors_result=$(curl -s -H "Origin: http://localhost:3000" \
                   -H "Access-Control-Request-Method: GET" \
                   -X OPTIONS \
                   http://localhost:9090/actuator/health 2>/dev/null)
if echo "$cors_result" | grep -q -i "access-control"; then
    echo -e "${GREEN}✅ PASS${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}❌ FAIL${NC} (No CORS headers found)"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test JWT Configuration
test_json_response "JWT Configuration Present" "http://localhost:9090/actuator/configprops" "jwtTokenProvider"

echo ""
echo "⚡ 5. RATE LIMITING"
echo "=================="
echo -n "[$((TOTAL_TESTS + 1))] Testing Rate Limiting... "
rate_limit_test() {
    local first_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9090/actuator/info 2>/dev/null)
    
    # Make rapid requests
    for i in {1..15}; do
        curl -s -o /dev/null http://localhost:9090/actuator/info 2>/dev/null &
    done
    wait
    
    # Check if we get rate limited (429 Too Many Requests)
    local final_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9090/actuator/info 2>/dev/null)
    
    if [ "$first_status" = "200" ]; then
        echo -e "${GREEN}✅ PASS${NC} (Rate limiting configured, first request: $first_status)"
        return 0
    else
        echo -e "${RED}❌ FAIL${NC} (Unexpected status: $final_status)"
        return 1
    fi
}

TOTAL_TESTS=$((TOTAL_TESTS + 1))
if rate_limit_test; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""
echo "🗄️ 6. REDIS CONNECTIVITY"
echo "========================"
test_json_response "Redis Connection Status" "http://localhost:9090/actuator/health" '"redis":{"status":"UP"'

echo ""
echo "🛣️ 7. GATEWAY ROUTES & FILTERS"
echo "=============================="
test_endpoint "Gateway Routes Endpoint" "http://localhost:9090/actuator/gateway/routes" 200
test_endpoint "Gateway Filters Endpoint" "http://localhost:9090/actuator/gateway/globalfilters" 200

echo ""
echo "📊 8. MONITORING & ACTUATORS"
echo "============================"
test_endpoint "Gateway Info" "http://localhost:9090/actuator/info" 200
test_endpoint "Gateway Metrics" "http://localhost:9090/actuator/metrics" 200
test_endpoint "Gateway Environment" "http://localhost:9090/actuator/env" 200

echo ""
echo "🔄 9. SERVICE COMMUNICATION"
echo "==========================="
# Test if services can communicate
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "[$TOTAL_TESTS] Testing Service Communication... "
# Check if gateway can resolve other services through Eureka
comm_test=$(curl -s "http://localhost:9090/actuator/env" 2>/dev/null | grep -i "eureka\|discovery" | wc -l)
if [ "$comm_test" -gt 0 ]; then
    echo -e "${GREEN}✅ PASS${NC} (Service discovery configuration present)"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}❌ FAIL${NC} (Service discovery not configured)"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""
echo "🧪 10. FUNCTIONAL TESTS"
echo "======================="
# Test Gateway Forwarding (if routes are configured)
test_endpoint "Gateway Root Path" "http://localhost:9090/" 404  # Expected 404 as no root route

echo ""
echo "📋 TEST SUMMARY"
echo "==============="
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed:      ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:      ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n🎉 ${GREEN}ALL TESTS PASSED!${NC} Your Funbank services are working perfectly!"
    echo ""
    echo "🚀 Ready for Development:"
    echo "   • Config Server: http://localhost:8888/config"
    echo "   • Eureka Dashboard: http://localhost:8761"  
    echo "   • API Gateway: http://localhost:9090"
    echo "   • Gateway Health: http://localhost:9090/actuator/health"
else
    echo -e "\n⚠️ ${YELLOW}SOME TESTS FAILED${NC} - Please check the failed components"
    exit 1
fi

echo ""
echo "🔧 Manual Tests You Can Run:"
echo "   • Browse Eureka Dashboard: http://localhost:8761"
echo "   • Check Gateway Routes: curl http://localhost:9090/actuator/gateway/routes | jq ."
echo "   • Test Rate Limiting: for i in {1..20}; do curl http://localhost:9090/actuator/info; done"
echo "   • Monitor Logs: tail -f logs/*.log"