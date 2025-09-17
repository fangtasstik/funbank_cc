#!/bin/bash

echo "🔍 OpenTelemetry Status Check"
echo "============================"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo ""
echo "1️⃣ Checking OTel Dependencies"
echo "-----------------------------"
if find . -name "pom.xml" -exec grep -l "opentelemetry" {} \; | head -3; then
    echo -e "${GREEN}✅ OTel dependencies found in POM files${NC}"
else
    echo -e "${RED}❌ No OTel dependencies found${NC}"
fi

echo ""
echo "2️⃣ Checking Gateway OTel Configuration"
echo "-------------------------------------"
if curl -s http://localhost:9090/actuator/metrics | grep -i "otel\|trace\|span" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ OTel metrics available${NC}"
else
    echo -e "${RED}❌ No OTel metrics found${NC}"
fi

echo ""
echo "3️⃣ Checking Tracing Headers"
echo "---------------------------"
headers=$(curl -s -v http://localhost:9090/actuator/health 2>&1 | grep -i "trace\|span\|correlation" || echo "No tracing headers")
if [[ "$headers" != "No tracing headers" ]]; then
    echo -e "${GREEN}✅ Tracing headers present:${NC}"
    echo "$headers"
else
    echo -e "${YELLOW}⚠️ No OTel tracing headers (using manual correlation IDs)${NC}"
fi

echo ""
echo "4️⃣ Checking for OTel Java Agent"
echo "------------------------------"
jps_output=$(jps -v 2>/dev/null | grep -i "javaagent.*opentelemetry" || echo "No OTel agent found")
if [[ "$jps_output" != "No OTel agent found" ]]; then
    echo -e "${GREEN}✅ OTel Java agent active:${NC}"
    echo "$jps_output"
else
    echo -e "${RED}❌ No OTel Java agent running${NC}"
fi

echo ""
echo "5️⃣ Checking Jaeger Availability"
echo "------------------------------"
if curl -s http://localhost:16686/api/services >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Jaeger UI accessible at http://localhost:16686${NC}"
else
    echo -e "${RED}❌ Jaeger not available (check docker-compose)${NC}"
fi

echo ""
echo "6️⃣ Checking Manual Correlation IDs"
echo "---------------------------------"
correlation_test=$(curl -s -v http://localhost:9090/actuator/health 2>&1 | grep -i "x-correlation-id" || echo "No correlation ID")
if [[ "$correlation_test" != "No correlation ID" ]]; then
    echo -e "${GREEN}✅ Manual correlation ID tracking working${NC}"
else
    echo -e "${YELLOW}⚠️ Correlation IDs may not be active${NC}"
fi

echo ""
echo "7️⃣ OTel Environment Variables"
echo "----------------------------"
env_vars=$(env | grep -i "otel\|trace" || echo "No OTel environment variables")
if [[ "$env_vars" != "No OTel environment variables" ]]; then
    echo -e "${GREEN}✅ OTel environment variables:${NC}"
    echo "$env_vars"
else
    echo -e "${RED}❌ No OTel environment variables set${NC}"
fi

echo ""
echo "📊 SUMMARY"
echo "=========="
echo -e "${YELLOW}Current Status: Manual tracing with correlation IDs${NC}"
echo -e "${YELLOW}OTel Status: Configured but not active${NC}"
echo ""
echo "🛠️ To Enable Full OpenTelemetry:"
echo "1. Add OTel Spring Boot starter dependency"
echo "2. Configure OTLP exporter properties"
echo "3. Start Jaeger container"
echo "4. Add OTel Java agent (optional)"
echo ""
echo "📖 Manual Tests:"
echo "• Make request with correlation ID:"
echo "  curl -H 'X-Correlation-ID: test-123' http://localhost:9090/actuator/health"
echo "• Check gateway logs for correlation ID propagation"
echo "• Start Jaeger: docker-compose -f docker-compose.test.yml up jaeger-test"