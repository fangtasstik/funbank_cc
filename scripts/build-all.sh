#!/bin/bash

# Funbank Multi-Module Build Script
# Builds all microservices in dependency order

echo "=========================================="
echo "Funbank Banking System - Multi-Module Build"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH"
    exit 1
fi

print_status "Java version: $(java -version 2>&1 | head -n 1)"
print_status "Maven version: $(mvn -version 2>&1 | head -n 1)"

# Build order (respecting dependencies)
MODULES=(
    "funbank-common"
    "funbank-config-server" 
    "funbank-service-registry"
    "funbank-api-gateway"
)

# Function to build a single module
build_module() {
    local module=$1
    print_status "Building module: $module"
    
    cd "$module" || {
        print_error "Cannot change to directory: $module"
        return 1
    }
    
    if mvn clean compile -q; then
        print_status "✓ Successfully built: $module"
        cd ..
        return 0
    else
        print_error "✗ Failed to build: $module"
        cd ..
        return 1
    fi
}

# Main build process
main() {
    print_status "Starting multi-module build..."
    
    # First, validate parent POM
    print_status "Validating parent POM..."
    if mvn validate -q; then
        print_status "✓ Parent POM is valid"
    else
        print_error "✗ Parent POM validation failed"
        exit 1
    fi
    
    # Build each module
    failed_modules=()
    
    for module in "${MODULES[@]}"; do
        if [[ -d "$module" ]]; then
            if ! build_module "$module"; then
                failed_modules+=("$module")
            fi
        else
            print_warning "Module directory not found: $module"
        fi
    done
    
    # Summary
    echo
    echo "=========================================="
    echo "Build Summary"
    echo "=========================================="
    
    if [[ ${#failed_modules[@]} -eq 0 ]]; then
        print_status "✓ All modules built successfully!"
        
        print_status "Building entire project from root..."
        if mvn clean compile -q; then
            print_status "✓ Complete project build successful!"
        else
            print_error "✗ Complete project build failed"
            exit 1
        fi
    else
        print_error "✗ Failed modules: ${failed_modules[*]}"
        exit 1
    fi
}

# Execute main function
main "$@"