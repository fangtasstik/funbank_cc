#!/bin/bash

echo "=== Funbank Services Usage Guide ==="
echo ""

echo "🔍 1. Check Service Health"
echo "User Service:"
curl -s http://localhost:9091/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo "Auth Service:"
curl -s http://localhost:9092/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo ""

echo "🔑 2. Auth Service Endpoints"
echo ""
echo "📋 Get Auth Service Info:"
curl -s http://localhost:9092/api/auth/info 2>/dev/null || echo "Response: 401 (Authentication required - Spring Security active)"
echo ""

echo "🔐 Login Attempt (should work when security is disabled):"
echo "curl -X POST http://localhost:9092/api/auth/login \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"username\":\"admin\",\"password\":\"password\"}'"
echo ""

echo "👥 3. User Service Endpoints"
echo ""
echo "📋 Get All Users:"
curl -s http://localhost:9091/api/users 2>/dev/null || echo "Response: 401 (Authentication required - Spring Security active)"
echo ""

echo "➕ Create User (example):"
echo "curl -X POST http://localhost:9091/api/users \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{"
echo "    \"username\": \"john.doe\","
echo "    \"email\": \"john@example.com\","
echo "    \"firstName\": \"John\","
echo "    \"lastName\": \"Doe\","
echo "    \"phoneNumber\": \"123-456-7890\","
echo "    \"status\": \"ACTIVE\""
echo "  }'"
echo ""

echo "🔍 Search Users (example):"
echo "curl 'http://localhost:9091/api/users/search?term=john'"
echo ""

echo "🌐 4. Through API Gateway"
echo ""
echo "📋 Access via Gateway (User Service):"
echo "curl http://localhost:9090/api/users"
echo ""
echo "🔐 Access via Gateway (Auth Service):"
echo "curl http://localhost:9090/api/auth/info"
echo ""

echo "📊 5. Database Status"
echo "User Service Database: $(curl -s http://localhost:9091/actuator/health | grep -o '"db":{"status":"[^"]*"' | cut -d'"' -f4)"
echo ""

echo "ℹ️  Note: Services currently have Spring Security enabled, requiring authentication."
echo "ℹ️  In production, you would:"
echo "   1. Login via Auth Service to get JWT token"
echo "   2. Include 'Authorization: Bearer <token>' header in requests"
echo "   3. Use the API Gateway for all external requests"
echo ""
echo "📖 See docs/SERVICE_USAGE_GUIDE.md for detailed usage instructions"
echo "📋 Service logs are available in logs/ directory"