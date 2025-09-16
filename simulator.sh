#!/bin/bash

# Cisco ISE AI Network Simulator Startup Script
# Usage: ./simulator.sh [OPTIONS]

# Default values
INTERVAL=30
DEVICES=50
SCENARIO="office"
PROFILE="simulator"
JAR_FILE="target/intelligent-policy-management-1.0-SNAPSHOT.jar"
LOG_LEVEL="INFO"
JAVA_OPTS="-Xmx2g -Xms1g"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "${BLUE}================================================================================================${NC}"
    echo -e "${CYAN}                           🚀 CISCO ISE AI NETWORK SIMULATOR 🚀                              ${NC}"
    echo -e "${BLUE}================================================================================================${NC}"
}

print_info() {
    echo -e "${GREEN}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to show help
show_help() {
    print_header
    echo
    echo -e "${CYAN}DESCRIPTION:${NC}"
    echo "  Cisco ISE AI Network Simulator generates realistic network data for demonstration"
    echo "  of AI-powered policy management capabilities."
    echo
    echo -e "${CYAN}USAGE:${NC}"
    echo "  ./simulator.sh [OPTIONS]"
    echo
    echo -e "${CYAN}OPTIONS:${NC}"
    echo "  --interval=N     Update interval in seconds (default: 30, min: 5)"
    echo "  --devices=N      Number of devices to simulate (default: 50, max: 1000)"
    echo "  --scenario=TYPE  Network scenario type (default: office)"
    echo "  --log-level=LVL  Logging level: DEBUG, INFO, WARN, ERROR (default: INFO)"
    echo "  --java-opts=OPTS Additional JVM options (default: -Xmx2g -Xms1g)"
    echo "  --build          Build the project before running"
    echo "  --help, -h       Show this help message"
    echo
    echo -e "${CYAN}SCENARIOS:${NC}"
    echo "  office          Corporate office environment (default)"
    echo "  campus          University/educational campus"
    echo "  datacenter      Data center environment"
    echo "  retail          Retail store environment"
    echo "  healthcare      Hospital/healthcare facility"
    echo "  manufacturing   Manufacturing plant"
    echo
    echo -e "${CYAN}EXAMPLES:${NC}"
    echo "  ./simulator.sh --interval=15 --devices=100"
    echo "  ./simulator.sh --scenario=campus --devices=200"
    echo "  ./simulator.sh --interval=60 --scenario=datacenter --log-level=DEBUG"
    echo "  ./simulator.sh --build --interval=30 --devices=75 --scenario=healthcare"
    echo
    echo -e "${CYAN}MONITORING:${NC}"
    echo "  Simulator API:     http://localhost:8080/simulator/status"
    echo "  Health Check:      http://localhost:8080/simulator/health"
    echo "  Admin Dashboard:   http://localhost:3000 (if React app is running)"
    echo "  H2 Console:        http://localhost:8080/h2-console"
    echo
    echo -e "${CYAN}CONTROL:${NC}"
    echo "  Press Ctrl+C to stop the simulator gracefully"
    echo
}

# Function to validate parameters
validate_params() {
    if [ "$INTERVAL" -lt 5 ]; then
        print_error "Interval must be at least 5 seconds"
        exit 1
    fi
    
    if [ "$DEVICES" -lt 10 ] || [ "$DEVICES" -gt 1000 ]; then
        print_error "Device count must be between 10 and 1000"
        exit 1
    fi
    
    case "$SCENARIO" in
        office|campus|datacenter|retail|healthcare|manufacturing)
            ;;
        *)
            print_error "Invalid scenario: $SCENARIO"
            print_info "Valid scenarios: office, campus, datacenter, retail, healthcare, manufacturing"
            exit 1
            ;;
    esac
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required (found Java $JAVA_VERSION)"
        exit 1
    fi
    print_success "Java $JAVA_VERSION found"
    
    # Check if JAR file exists
    if [ ! -f "$JAR_FILE" ]; then
        print_warning "JAR file not found: $JAR_FILE"
        print_info "Building the project..."
        build_project
    fi
}

# Function to build the project
build_project() {
    print_info "Building Cisco ISE AI project..."
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Project built successfully"
    else
        print_error "Failed to build project"
        exit 1
    fi
}

# Function to start the simulator
start_simulator() {
    print_header
    echo
    print_info "Starting Cisco ISE AI Network Simulator..."
    echo
    print_info "Configuration:"
    echo "  📊 Update Interval: ${INTERVAL} seconds"
    echo "  📱 Device Count: ${DEVICES}"
    echo "  🏢 Scenario: ${SCENARIO}"
    echo "  📝 Log Level: ${LOG_LEVEL}"
    echo "  ☕ Java Options: ${JAVA_OPTS}"
    echo
    print_info "Endpoints:"
    echo "  🌐 Simulator API: http://localhost:8080/simulator/status"
    echo "  ❤️  Health Check: http://localhost:8080/simulator/health"
    echo "  📊 Admin Portal: http://localhost:3000 (if running)"
    echo
    print_info "Press Ctrl+C to stop the simulator"
    echo
    print_success "Simulator starting..."
    echo
    
    # Start the simulator
    java $JAVA_OPTS \
        -Dspring.profiles.active=$PROFILE \
        -Dsimulator.interval=$INTERVAL \
        -Dsimulator.device-count=$DEVICES \
        -Dsimulator.scenario=$SCENARIO \
        -Dlogging.level.com.cisco.ise.ai.simulator=$LOG_LEVEL \
        -jar "$JAR_FILE" \
        --interval=$INTERVAL \
        --devices=$DEVICES \
        --scenario=$SCENARIO
}

# Parse command line arguments
BUILD_PROJECT=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --interval=*)
            INTERVAL="${1#*=}"
            shift
            ;;
        --devices=*)
            DEVICES="${1#*=}"
            shift
            ;;
        --scenario=*)
            SCENARIO="${1#*=}"
            shift
            ;;
        --log-level=*)
            LOG_LEVEL="${1#*=}"
            shift
            ;;
        --java-opts=*)
            JAVA_OPTS="${1#*=}"
            shift
            ;;
        --build)
            BUILD_PROJECT=true
            shift
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Main execution
main() {
    # Validate parameters
    validate_params
    
    # Check prerequisites
    check_prerequisites
    
    # Build project if requested
    if [ "$BUILD_PROJECT" = true ]; then
        build_project
    fi
    
    # Start the simulator
    start_simulator
}

# Trap Ctrl+C for graceful shutdown
trap 'echo -e "\n${YELLOW}🛑 Shutting down simulator...${NC}"; exit 0' INT

# Run main function
main
