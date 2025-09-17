#!/bin/bash

echo "🔍 OpenTelemetry Verification for Funbank API Gateway"
echo "===================================================="
echo ""

# Check if Jaeger is running
echo "1. Checking Jaeger availability..."
if curl -s http://localhost:16686/api/services > /dev/null 2>&1; then
    echo "   ✅ Jaeger is running at http://localhost:16686"
else
    echo "   ❌ Jaeger is not available"
    exit 1
fi

# Check if gateway is running
echo "2. Checking API Gateway status..."
if curl -s http://localhost:9090/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Gateway is running at http://localhost:9090"
else
    echo "   ❌ Gateway is not available"
    exit 1
fi

# Check if service is registered in Jaeger
echo "3. Checking service registration in Jaeger..."
services=$(curl -s http://localhost:16686/api/services | grep -o 'funbank-api-gateway')
if [[ -n "$services" ]]; then
    echo "   ✅ Service 'funbank-api-gateway' found in Jaeger"
else
    echo "   ❌ Service not found in Jaeger"
    exit 1
fi

# Make some test requests to generate traces
echo "4. Generating test traces..."
curl -s http://localhost:9090/actuator/health > /dev/null
curl -s http://localhost:9090/actuator/info > /dev/null
curl -s http://localhost:9090/actuator/metrics > /dev/null
curl -s http://localhost:9090/test-404 > /dev/null
echo "   ✅ Test requests sent"

# Wait a bit for traces to be processed
sleep 3

# Check if traces exist
echo "5. Checking trace collection..."
trace_count=$(curl -s "http://localhost:16686/api/traces?service=funbank-api-gateway&limit=50" | wc -c)
if [[ $trace_count -gt 1000 ]]; then
    echo "   ✅ Traces collected ($trace_count characters of trace data)"
else
    echo "   ❌ No significant traces found"
fi

echo ""
echo "🎉 OpenTelemetry Verification Complete!"
echo ""
echo "📊 View traces in Jaeger UI: http://localhost:16686"
echo "🔍 Search for service: funbank-api-gateway"
echo ""
echo "🧪 Test commands:"
echo "   curl http://localhost:9090/actuator/health"
echo "   curl http://localhost:9090/actuator/metrics"
echo ""