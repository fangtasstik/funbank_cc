#!/bin/bash

echo "🔍 File Organization Compliance Check"
echo "====================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

violations=0
total_checks=0

# Function to check for violations
check_violation() {
    local pattern="$1"
    local location="$2" 
    local should_be="$3"
    local description="$4"
    
    total_checks=$((total_checks + 1))
    
    if find . -maxdepth 1 -name "$pattern" -not -name "README.md" 2>/dev/null | grep -q .; then
        violations=$((violations + 1))
        echo -e "${RED}❌ VIOLATION:${NC} $description"
        echo -e "   Found: $(find . -maxdepth 1 -name "$pattern" -not -name "README.md" 2>/dev/null | head -3)"
        echo -e "   Should be in: $should_be"
        echo ""
    else
        echo -e "${GREEN}✅ COMPLIANT:${NC} $description"
    fi
}

echo "Checking file organization rules..."
echo ""

# Check for .md files in root (except README.md)
check_violation "*.md" "root" "docs/" "Documentation files (.md) in root directory"

# Check for .sh files in root
check_violation "*.sh" "root" "scripts/" "Shell scripts (.sh) in root directory"

# Check for .log files in root  
check_violation "*.log" "root" "logs/" "Log files (.log) in root directory"

# Check for .sh files in service directories
echo -e "${YELLOW}Checking service directories for misplaced files...${NC}"
for dir in funbank-*; do
    if [ -d "$dir" ]; then
        if find "$dir" -name "*.sh" 2>/dev/null | grep -q .; then
            violations=$((violations + 1))
            echo -e "${RED}❌ VIOLATION:${NC} Shell scripts in service directory $dir"
            echo -e "   Found: $(find "$dir" -name "*.sh" 2>/dev/null)"
            echo -e "   Should be in: scripts/"
            echo ""
        fi
        
        if find "$dir" -name "*.log" 2>/dev/null | grep -q .; then
            violations=$((violations + 1))
            echo -e "${RED}❌ VIOLATION:${NC} Log files in service directory $dir"
            echo -e "   Found: $(find "$dir" -name "*.log" 2>/dev/null)"
            echo -e "   Should be in: logs/"
            echo ""
        fi
    fi
done

# Summary
echo ""
echo "📊 COMPLIANCE SUMMARY"
echo "===================="
echo "Total checks: $total_checks"
echo "Violations: $violations"

if [ $violations -eq 0 ]; then
    echo -e "${GREEN}🎉 PROJECT IS FULLY COMPLIANT!${NC}"
    echo ""
    echo "File organization follows the rules in rules/file_organization_rule.md:"
    echo "  📖 Documentation (.md) → docs/"
    echo "  🔧 Shell scripts (.sh) → scripts/"  
    echo "  📋 Log files (.log) → logs/"
    exit 0
else
    echo -e "${RED}⚠️  PROJECT HAS $violations VIOLATION(S)${NC}"
    echo ""
    echo "Please fix violations by:"
    echo "  1. Moving files to correct directories"
    echo "  2. Updating references in scripts and documentation"
    echo "  3. Testing affected functionality"
    echo ""
    echo "See rules/file_organization_rule.md for complete guidelines."
    exit 1
fi