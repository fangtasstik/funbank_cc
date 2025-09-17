#\!/bin/bash

# Funbank Observability Stack Verification Script
# This script verifies that all observability components are healthy and working

echo "🔍 Funbank Observability Stack Verification"
echo "==========================================="
echo ""

# Colors for output
GREEN='\\033[0;32m'
RED='\\033[0;31m'
YELLOW='\\033[1;33m'
NC='\\033[0m' # No Color

# Function to check if service is responding
check_service() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Checking $service_name... "
    
    # Use timeout to avoid hanging
    response=$(curl -s -w "%{http_code}" -o /dev/null --connect-timeout 5 --max-time 10 "$url" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✅ OK${NC}"
        return 0
    else
        echo -e "${RED}❌ FAILED (HTTP: $response)${NC}"
        return 1
    fi
}

# Check all service endpoints
echo "Checking service endpoints..."
echo "============================="

endpoints=(
    "API Gateway Health:http://localhost:8080/actuator/health"
    "API Gateway Metrics:http://localhost:8080/actuator/prometheus"
    "Prometheus:http://localhost:9090/-/healthy"
    "Grafana:http://localhost:3001/api/health" 
    "Elasticsearch:http://localhost:9200/_cluster/health"
    "Logstash:http://localhost:9600"
    "Kibana:http://localhost:5601/api/status"
    "Jaeger:http://localhost:16686"
)

all_ok=true
for endpoint in "${endpoints[@]}"; do
    service_name=$(echo "$endpoint"  < /dev/null |  cut -d: -f1)
    url=$(echo "$endpoint" | cut -d: -f2-)
    
    if \! check_service "$service_name" "$url"; then
        all_ok=false
    fi
done

echo ""
if [ "$all_ok" = true ]; then
    echo -e "${GREEN}🎉 All observability services are healthy\!${NC}"
    echo ""
    echo "Access your monitoring stack:"
    echo "• Grafana (Dashboards):     http://localhost:3001 (admin/funbank_admin_2024)"
    echo "• Prometheus (Metrics):     http://localhost:9090"
    echo "• Kibana (Logs):           http://localhost:5601"  
    echo "• Jaeger (Tracing):        http://localhost:16686"
    echo "• API Gateway (Actuator):  http://localhost:8080/actuator"
else
    echo -e "${RED}❌ Some services are not healthy. Run 'docker-compose up -d' and try again.${NC}"
fi
