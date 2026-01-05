#!/bin/bash
# Project Forge - Setup Script
# This script sets up the local development environment

set -e

echo "🔥 Project Forge - Setup Script"
echo "================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is not installed"
        return 1
    fi
}

MISSING=0
check_command docker || MISSING=1
check_command docker-compose || MISSING=1
check_command java || echo -e "${YELLOW}⚠${NC} Java not found (optional for local build)"
check_command mvn || echo -e "${YELLOW}⚠${NC} Maven not found (optional for local build)"
check_command terraform || echo -e "${YELLOW}⚠${NC} Terraform not found (optional for infrastructure)"
check_command kubectl || echo -e "${YELLOW}⚠${NC} kubectl not found (optional for Kubernetes)"
check_command aws || echo -e "${YELLOW}⚠${NC} AWS CLI not found (optional for AWS deployment)"

if [ $MISSING -eq 1 ]; then
    echo -e "\n${RED}Error: Required tools are missing. Please install them first.${NC}"
    exit 1
fi

# Build Java SDK
echo -e "\n${YELLOW}Building Java Observability SDK...${NC}"
if command -v mvn &> /dev/null; then
    cd java-observability-sdk
    mvn clean install -DskipTests -q
    cd ..
    echo -e "${GREEN}✓${NC} SDK built successfully"
else
    echo -e "${YELLOW}⚠${NC} Skipping SDK build (Maven not found)"
fi

# Build Sample Microservice
echo -e "\n${YELLOW}Building Sample Microservice...${NC}"
if command -v mvn &> /dev/null; then
    cd sample-microservice
    mvn clean package -DskipTests -q
    cd ..
    echo -e "${GREEN}✓${NC} Microservice built successfully"
else
    echo -e "${YELLOW}⚠${NC} Skipping microservice build (Maven not found)"
fi

# Create Docker network if not exists
echo -e "\n${YELLOW}Setting up Docker network...${NC}"
docker network create project-forge 2>/dev/null || true
echo -e "${GREEN}✓${NC} Docker network ready"

# Start services
echo -e "\n${YELLOW}Starting observability stack...${NC}"
cd docker
docker-compose up -d
cd ..

echo -e "\n${GREEN}✓${NC} Setup complete!"
echo ""
echo "Services available at:"
echo "  - Sample Microservice: http://localhost:8080"
echo "  - Prometheus:          http://localhost:9090"
echo "  - Grafana:             http://localhost:3000 (admin/admin)"
echo "  - Kibana:              http://localhost:5601"
echo "  - Elasticsearch:       http://localhost:9200"
echo ""
echo "Useful endpoints:"
echo "  - Health:    http://localhost:8080/actuator/health"
echo "  - Metrics:   http://localhost:8080/actuator/prometheus"
echo "  - API:       http://localhost:8080/api/orders"
echo ""
echo "To view logs: docker-compose -f docker/docker-compose.yml logs -f"
echo "To stop:      docker-compose -f docker/docker-compose.yml down"

