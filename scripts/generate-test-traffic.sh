#\!/bin/bash

# Generate realistic banking test traffic for observability testing

echo "🏦 Generating Funbank Test Traffic"
echo "================================="
echo ""

API_BASE="http://localhost:8080"

# Colors
GREEN='\\033[0;32m'
YELLOW='\\033[1;33m'
NC='\\033[0m'

echo -e "${YELLOW}Generating realistic banking API traffic...${NC}"

# Function to make API calls with realistic delays
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -n "  $description... "
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "%{http_code}" -o /dev/null -X $method "$API_BASE$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" 2>/dev/null)
    else
        response=$(curl -s -w "%{http_code}" -o /dev/null -X $method "$API_BASE$endpoint" 2>/dev/null)
    fi
    
    echo "HTTP $response"
    sleep 0.5
}

echo "1. Health and System Checks"
make_request "GET" "/actuator/health" "" "Health check"
make_request "GET" "/actuator/metrics" "" "Metrics endpoint"
make_request "GET" "/actuator/prometheus" "" "Prometheus metrics"

echo ""
echo "2. Authentication Scenarios"
for i in {1..5}; do
    make_request "POST" "/auth/login" '{"username":"user$i","password":"password123"}' "Login attempt $i"
    sleep 1
done

# Simulate some failed auth attempts
for i in {1..3}; do
    make_request "POST" "/auth/login" '{"username":"baduser$i","password":"wrongpass"}' "Failed login $i"
    sleep 0.5
done

echo ""
echo "3. Banking Transaction Simulation"
for i in {1..8}; do
    make_request "GET" "/transactions/user/$((123 + i))" "" "Get user $((123 + i)) transactions"
    make_request "POST" "/transactions" '{"amount":$((100 + i * 50)),"type":"TRANSFER","fromAccount":"ACC$((1000 + i))","toAccount":"ACC$((2000 + i))"}' "Transfer \$$((100 + i * 50))"
    make_request "GET" "/accounts/$((1000 + i))/balance" "" "Check account balance"
    sleep 2
done

echo ""
echo "4. Security and Rate Limiting Test"
echo "  Rapid requests to trigger rate limiting..."
for i in {1..15}; do
    curl -s "$API_BASE/auth/validate-token" >/dev/null 2>&1 &
done
wait

echo ""
echo "5. Various Endpoint Coverage"
make_request "GET" "/users/profile/123" "" "Get user profile"
make_request "POST" "/accounts" '{"userId":123,"type":"SAVINGS","currency":"USD"}' "Create account"
make_request "GET" "/cards/user/123" "" "Get user cards"
make_request "POST" "/notifications" '{"userId":123,"type":"TRANSACTION","message":"Transfer completed"}' "Send notification"

echo ""
echo -e "${GREEN}✅ Test traffic generation completed\!${NC}"
echo ""
echo "Now check your dashboards:"
echo "• Grafana: http://localhost:3001 (admin/funbank_admin_2024)"
echo "• Prometheus: http://localhost:9090"
echo "• Kibana: http://localhost:5601"
echo "• Jaeger: http://localhost:16686"
echo ""
echo "You should see:"
echo "• Request rates and response times in Grafana"
echo "• HTTP status code distribution"
echo "• Authentication success/failure metrics"
echo "• Transaction volume data"
echo "• Security events and rate limiting"
