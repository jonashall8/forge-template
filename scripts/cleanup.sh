#!/bin/bash
# Project Forge - Cleanup Script
# This script tears down the infrastructure and cleans up resources

set -e

echo "🔥 Project Forge - Cleanup Script"
echo "=================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
ENVIRONMENT=${1:-dev}
REGION=${AWS_REGION:-us-east-1}

echo -e "\n${RED}WARNING: This will destroy all resources in ${ENVIRONMENT}!${NC}"
echo -e "${RED}This action cannot be undone.${NC}"
echo ""
read -p "Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Cleanup cancelled."
    exit 0
fi

echo -e "\n${YELLOW}Starting cleanup for ${ENVIRONMENT}...${NC}"

# Delete Kubernetes resources first
echo -e "\n${YELLOW}Deleting Kubernetes resources...${NC}"

if kubectl cluster-info &> /dev/null; then
    kubectl delete -f kubernetes/sample-app/ --ignore-not-found=true || true
    kubectl delete -f kubernetes/grafana/ --ignore-not-found=true || true
    kubectl delete -f kubernetes/prometheus/ --ignore-not-found=true || true
    kubectl delete -f kubernetes/elk-stack/ --ignore-not-found=true || true
    kubectl delete -f kubernetes/namespaces/ --ignore-not-found=true || true
    echo -e "${GREEN}✓${NC} Kubernetes resources deleted"
else
    echo -e "${YELLOW}⚠${NC} Kubernetes cluster not accessible, skipping"
fi

# Destroy Terraform infrastructure
echo -e "\n${YELLOW}Destroying Terraform infrastructure...${NC}"
cd terraform/environments/${ENVIRONMENT}

if [ -f "terraform.tfstate" ] || terraform state list &> /dev/null 2>&1; then
    terraform destroy -auto-approve
    echo -e "${GREEN}✓${NC} Infrastructure destroyed"
else
    echo -e "${YELLOW}⚠${NC} No Terraform state found, skipping"
fi
cd ../../..

# Clean up local Docker resources
echo -e "\n${YELLOW}Cleaning up local Docker resources...${NC}"
cd docker
docker-compose down -v --remove-orphans 2>/dev/null || true
cd ..

# Remove Docker images
docker rmi $(docker images -q "*/sample-microservice" 2>/dev/null) 2>/dev/null || true
docker rmi $(docker images -q "sample-microservice" 2>/dev/null) 2>/dev/null || true

echo -e "${GREEN}✓${NC} Docker resources cleaned"

# Clean up ECR images (optional)
echo -e "\n${YELLOW}Do you want to delete ECR images? (yes/no): ${NC}"
read -p "" DELETE_ECR

if [ "$DELETE_ECR" == "yes" ]; then
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    REGISTRY="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
    
    echo "Deleting ECR images..."
    aws ecr batch-delete-image \
        --repository-name sample-microservice \
        --image-ids "$(aws ecr list-images --repository-name sample-microservice --query 'imageIds[*]' --output json)" \
        2>/dev/null || true
    
    echo -e "${GREEN}✓${NC} ECR images deleted"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Cleanup Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Note: The Terraform state backend (S3 bucket and DynamoDB table)"
echo "was NOT deleted. To remove it manually:"
echo ""
echo "  aws s3 rb s3://project-forge-terraform-state --force"
echo "  aws dynamodb delete-table --table-name project-forge-terraform-locks"

