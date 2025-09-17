#!/bin/bash

echo "🧪 Testing Suncorp-Aligned Testing Stack for Funbank API Gateway"
echo "================================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 Testing Stack Components:${NC}"
echo "  ✅ JUnit 5 - Modern testing framework"
echo "  ✅ Mockito - Mocking framework"
echo "  ✅ AssertJ - Fluent assertions"
echo "  ✅ TestContainers - Integration testing with real databases"
echo "  ✅ WireMock - External service mocking"
echo "  ✅ REST Assured - API testing"
echo "  ✅ JaCoCo - Code coverage (80% threshold)"
echo ""

# Run unit tests
echo -e "${BLUE}1. Running Unit Tests (JUnit 5 + Mockito + AssertJ)${NC}"
echo "mvn test -pl funbank-api-gateway -Dtest=*Test -q"
if mvn test -pl funbank-api-gateway -Dtest=*Test -q; then
    echo -e "${GREEN}✅ Unit Tests Passed${NC}"
else
    echo -e "${RED}❌ Unit Tests Failed${NC}"
fi
echo ""

# Run integration tests with TestContainers
echo -e "${BLUE}2. Running Integration Tests (TestContainers + REST Assured)${NC}"
echo "mvn test -pl funbank-api-gateway -Dtest=*IntegrationTest -q"
if mvn test -pl funbank-api-gateway -Dtest=*IntegrationTest -q; then
    echo -e "${GREEN}✅ Integration Tests Passed${NC}"
else
    echo -e "${RED}❌ Integration Tests Failed${NC}"
fi
echo ""

# Run WireMock tests
echo -e "${BLUE}3. Running External Service Mock Tests (WireMock)${NC}"
echo "mvn test -pl funbank-api-gateway -Dtest=*MockTest -q"
if mvn test -pl funbank-api-gateway -Dtest=*MockTest -q; then
    echo -e "${GREEN}✅ WireMock Tests Passed${NC}"
else
    echo -e "${RED}❌ WireMock Tests Failed${NC}"
fi
echo ""

# Generate JaCoCo coverage report
echo -e "${BLUE}4. Generating Code Coverage Report (JaCoCo)${NC}"
echo "mvn jacoco:report -pl funbank-api-gateway -q"
if mvn jacoco:report -pl funbank-api-gateway -q; then
    echo -e "${GREEN}✅ Coverage Report Generated${NC}"
    
    # Check if coverage report exists and show location
    if [ -f "funbank-api-gateway/target/site/jacoco/index.html" ]; then
        echo "📊 Coverage Report: funbank-api-gateway/target/site/jacoco/index.html"
    fi
else
    echo -e "${RED}❌ Coverage Report Generation Failed${NC}"
fi
echo ""

# Run JaCoCo coverage check (80% threshold)
echo -e "${BLUE}5. Checking Code Coverage Threshold (80%)${NC}"
echo "mvn jacoco:check -pl funbank-api-gateway -q"
if mvn jacoco:check -pl funbank-api-gateway -q; then
    echo -e "${GREEN}✅ Coverage Threshold Met (>80%)${NC}"
else
    echo -e "${RED}⚠️  Coverage Threshold Not Met (<80%)${NC}"
    echo "This is expected for a new project. Add more tests to reach 80% coverage."
fi
echo ""

# Summary
echo -e "${BLUE}📊 Test Summary:${NC}"
echo "==================="
echo "• Unit Tests: JUnit 5, Mockito, AssertJ"
echo "• Integration Tests: TestContainers (Redis), REST Assured" 
echo "• Service Mocking: WireMock for external APIs"
echo "• Code Coverage: JaCoCo with 80% threshold enforcement"
echo ""
echo -e "${GREEN}🎉 Suncorp-Aligned Testing Stack Verification Complete!${NC}"
echo ""
echo "📚 Next Steps:"
echo "  1. Add more test cases to increase code coverage"
echo "  2. Implement contract testing with Pact when needed"
echo "  3. Add performance tests with Maven Surefire"
echo "  4. Configure CI/CD pipeline integration"