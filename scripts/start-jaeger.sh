#!/bin/bash

echo "🚀 Starting Jaeger for OpenTelemetry Tracing"
echo "============================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Jaeger is already running
if docker ps | grep -q jaeger; then
    echo "⚠️ Jaeger container already running. Stopping existing container..."
    docker stop $(docker ps -q --filter "name=jaeger")
    docker rm $(docker ps -aq --filter "name=jaeger")
fi

echo "🐳 Starting Jaeger All-in-One container..."

# Start Jaeger with OTLP support
docker run -d \
    --name jaeger \
    -p 16686:16686 \
    -p 4317:4317 \
    -p 4318:4318 \
    -p 14250:14250 \
    -e COLLECTOR_OTLP_ENABLED=true \
    jaegertracing/all-in-one:1.50

echo "⏳ Waiting for Jaeger to start..."
sleep 5

# Check if Jaeger is accessible
if curl -s http://localhost:16686/api/services > /dev/null 2>&1; then
    echo "✅ Jaeger started successfully!"
    echo ""
    echo "🌐 Jaeger UI: http://localhost:16686"
    echo "📡 OTLP HTTP Endpoint: http://localhost:4318"
    echo "📡 OTLP gRPC Endpoint: http://localhost:4317"
    echo ""
    echo "🧪 Test with:"
    echo "   curl http://localhost:16686/api/services"
else
    echo "❌ Failed to start Jaeger or service not accessible"
    echo "📋 Check container logs: docker logs jaeger"
fi

echo ""
echo "🛑 To stop Jaeger:"
echo "   docker stop jaeger && docker rm jaeger"