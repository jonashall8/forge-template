#!/bin/bash
# Project Forge - Deploy Script
# This script deploys the infrastructure and applications to AWS

set -e

echo "🔥 Project Forge - Deploy Script"
echo "================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
ENVIRONMENT=${1:-dev}
REGION=${AWS_REGION:-us-east-1}

echo -e "\n${YELLOW}Environment: ${ENVIRONMENT}${NC}"
echo -e "${YELLOW}Region: ${REGION}${NC}"

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

for cmd in aws terraform kubectl docker; do
    if ! command -v $cmd &> /dev/null; then
        echo -e "${RED}Error: $cmd is required but not installed.${NC}"
        exit 1
    fi
done

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}Error: AWS credentials not configured.${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} AWS credentials configured"

# Deploy Terraform State Backend (first time only)
echo -e "\n${YELLOW}Checking Terraform state backend...${NC}"
cd terraform/modules/state-backend

if ! aws s3 ls s3://project-forge-terraform-state 2>/dev/null; then
    echo "Creating Terraform state backend..."
    terraform init
    terraform apply -auto-approve \
        -var="bucket_name=project-forge-terraform-state" \
        -var="dynamodb_table_name=project-forge-terraform-locks"
    echo -e "${GREEN}✓${NC} State backend created"
else
    echo -e "${GREEN}✓${NC} State backend exists"
fi
cd ../../..

# Deploy Infrastructure
echo -e "\n${YELLOW}Deploying infrastructure for ${ENVIRONMENT}...${NC}"
cd terraform/environments/${ENVIRONMENT}

terraform init
terraform plan -out=tfplan
terraform apply tfplan

# Get cluster info
CLUSTER_NAME=$(terraform output -raw eks_cluster_name)
echo -e "${GREEN}✓${NC} Infrastructure deployed"
cd ../../..

# Update kubeconfig
echo -e "\n${YELLOW}Updating kubeconfig...${NC}"
aws eks update-kubeconfig --name ${CLUSTER_NAME} --region ${REGION}
echo -e "${GREEN}✓${NC} kubeconfig updated"

# Deploy Kubernetes resources
echo -e "\n${YELLOW}Deploying Kubernetes resources...${NC}"

# Create namespaces
kubectl apply -f kubernetes/namespaces/

# Deploy observability stack
echo "Deploying observability stack..."
kubectl apply -f kubernetes/elk-stack/
kubectl apply -f kubernetes/prometheus/
kubectl apply -f kubernetes/grafana/

# Wait for observability stack
echo "Waiting for observability stack to be ready..."
kubectl wait --for=condition=available deployment/elasticsearch -n observability --timeout=300s || true
kubectl wait --for=condition=available deployment/prometheus -n observability --timeout=300s || true
kubectl wait --for=condition=available deployment/grafana -n observability --timeout=300s || true

echo -e "${GREEN}✓${NC} Observability stack deployed"

# Build and push Docker image
echo -e "\n${YELLOW}Building and pushing Docker image...${NC}"
REGISTRY=$(aws sts get-caller-identity --query Account --output text).dkr.ecr.${REGION}.amazonaws.com
IMAGE_TAG=$(git rev-parse --short HEAD)

# Login to ECR
aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${REGISTRY}

# Build and push
cd sample-microservice
docker build -t ${REGISTRY}/sample-microservice:${IMAGE_TAG} .
docker push ${REGISTRY}/sample-microservice:${IMAGE_TAG}
cd ..

echo -e "${GREEN}✓${NC} Docker image pushed"

# Deploy application
echo -e "\n${YELLOW}Deploying application...${NC}"
sed -i "s|image:.*sample-microservice.*|image: ${REGISTRY}/sample-microservice:${IMAGE_TAG}|g" \
    kubernetes/sample-app/deployment.yaml

kubectl apply -f kubernetes/sample-app/
kubectl rollout status deployment/sample-microservice -n applications --timeout=300s

echo -e "${GREEN}✓${NC} Application deployed"

# Print summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Cluster: ${CLUSTER_NAME}"
echo "Region:  ${REGION}"
echo ""
echo "To access the cluster:"
echo "  kubectl get nodes"
echo "  kubectl get pods -A"
echo ""
echo "To port-forward services:"
echo "  kubectl port-forward svc/grafana -n observability 3000:3000"
echo "  kubectl port-forward svc/kibana -n observability 5601:5601"

