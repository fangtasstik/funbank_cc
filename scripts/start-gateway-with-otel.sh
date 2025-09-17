#!/bin/bash

echo "🚀 Starting Funbank API Gateway with Full OpenTelemetry"
echo "======================================================"

# Load environment variables from .env file
if [ -f .env ]; then
    echo "📋 Loading environment variables from .env file..."
    export $(cat .env | grep -v '#' | xargs)
else
    echo "⚠️ No .env file found - using defaults"
fi

# Check if Jaeger is running
echo "🔍 Checking Jaeger availability..."
if curl -s http://localhost:16686/api/services > /dev/null 2>&1; then
    echo "✅ Jaeger is running at http://localhost:16686"
else
    echo "❌ Jaeger not available. Starting Jaeger..."
    ./start-jaeger.sh
    sleep 5
fi

# Check if OpenTelemetry agent exists
if [ ! -f "opentelemetry-javaagent.jar" ]; then
    echo "❌ OpenTelemetry Java agent not found!"
    echo "💡 Download with: wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.32.0/opentelemetry-javaagent.jar"
    exit 1
fi

echo "🔧 OpenTelemetry Configuration:"
echo "   Service Name: ${OTEL_SERVICE_NAME:-funbank-api-gateway}"
echo "   Service Version: ${OTEL_SERVICE_VERSION:-1.0.0}"
echo "   OTLP Endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:-http://localhost:4318}"
echo "   Traces Exporter: ${OTEL_TRACES_EXPORTER:-otlp}"
echo "   Metrics Exporter: ${OTEL_METRICS_EXPORTER:-otlp}"

echo ""
echo "🚀 Starting API Gateway with OpenTelemetry Java Agent..."
echo "Press Ctrl+C to stop the application"
echo ""

# Start the gateway with OpenTelemetry Java Agent
java \
    -javaagent:./opentelemetry-javaagent.jar \
    -Dotel.service.name=${OTEL_SERVICE_NAME:-funbank-api-gateway} \
    -Dotel.service.version=${OTEL_SERVICE_VERSION:-1.0.0} \
    -Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://localhost:4318} \
    -Dotel.exporter.otlp.protocol=${OTEL_EXPORTER_OTLP_PROTOCOL:-http/protobuf} \
    -Dotel.traces.exporter=${OTEL_TRACES_EXPORTER:-otlp} \
    -Dotel.metrics.exporter=${OTEL_METRICS_EXPORTER:-otlp} \
    -Dotel.logs.exporter=${OTEL_LOGS_EXPORTER:-otlp} \
    -Dotel.resource.attributes="${OTEL_RESOURCE_ATTRIBUTES:-service.name=funbank-api-gateway,service.version=1.0.0,deployment.environment=dev}" \
    -Dotel.instrumentation.http.server.route-based-naming=true \
    -Dotel.instrumentation.spring-webflux.enabled=true \
    -Dotel.instrumentation.netty.enabled=true \
    -Dotel.instrumentation.reactor.enabled=true \
    -Dotel.propagators=tracecontext,baggage,b3,b3multi \
    -jar funbank-api-gateway/target/funbank-api-gateway-1.0.0.jar

echo ""
echo "🛑 Gateway stopped. To restart:"
echo "   ./start-gateway-with-otel.sh"