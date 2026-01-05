# Project Forge 

> A Cloud-Native Enterprise Reference Blueprint for Platform Engineering
## Overview

Project Forge is a template for building production-ready cloud-native applications. It provides:

- **Infrastructure as Code** - Terraform modules for AWS (VPC, EKS, ECS)
- **Observability SDK** - Java library with structured logging, metrics, and distributed tracing
- **Monitoring Stack** - Pre-configured ELK Stack, Prometheus, and Grafana
- **CI/CD Pipelines** - GitHub Actions workflows for build, scan, and deploy
- **Security-First Design** - Encrypted resources, private subnets, and DevSecOps practices

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        AWS Cloud                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    VPC (10.0.0.0/16)                       │  │
│  │  ┌─────────────────┐         ┌─────────────────┐          │  │
│  │  │  Public Subnet  │         │  Public Subnet  │          │  │
│  │  │   (10.0.1.0/24) │         │   (10.0.2.0/24) │          │  │
│  │  │   NAT Gateway   │         │   NAT Gateway   │          │  │
│  │  └────────┬────────┘         └────────┬────────┘          │  │
│  │           │                           │                    │  │
│  │  ┌────────▼────────┐         ┌────────▼────────┐          │  │
│  │  │ Private Subnet  │         │ Private Subnet  │          │  │
│  │  │  (10.0.10.0/24) │         │  (10.0.20.0/24) │          │  │
│  │  │                 │         │                 │          │  │
│  │  │  ┌───────────┐  │         │  ┌───────────┐  │          │  │
│  │  │  │    EKS    │  │         │  │    EKS    │  │          │  │
│  │  │  │   Nodes   │  │         │  │   Nodes   │  │          │  │
│  │  │  └───────────┘  │         │  └───────────┘  │          │  │
│  │  └─────────────────┘         └─────────────────┘          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   S3 Bucket     │  │   DynamoDB      │  │   CloudWatch    │  │
│  │ (Terraform State)│  │ (State Locking) │  │   (Logs)        │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

##  Project Structure

```
project-forge/
├── terraform/                    # Infrastructure as Code
│   ├── modules/
│   │   ├── networking/          # VPC, Subnets, NAT Gateway
│   │   ├── eks-cluster/         # Amazon EKS configuration
│   │   ├── ecs-cluster/         # Amazon ECS Fargate
│   │   └── state-backend/       # S3 + DynamoDB for Terraform state
│   └── environments/
│       ├── dev/                 # Development environment
│       └── prod/                # Production environment
├── java-observability-sdk/      # Reusable Java library
│   └── src/main/java/...        # Logging, Metrics, Tracing
├── sample-microservice/         # Example Spring Boot app
├── kubernetes/                  # K8s manifests
│   ├── elk-stack/              # Elasticsearch, Logstash, Kibana
│   ├── prometheus/             # Prometheus monitoring
│   └── grafana/                # Grafana dashboards
├── helm/                        # Helm charts
├── dashboards/                  # Grafana dashboard JSON
├── .github/workflows/           # CI/CD pipelines
├── docker/                      # Docker Compose files
└── scripts/                     # Utility scripts
```

##  Quick Start

### Prerequisites

- AWS CLI configured with appropriate credentials
- Terraform >= 1.5.0
- kubectl >= 1.28
- Docker >= 24.0
- Java 17+ and Maven 3.9+

### 1. Initialize Terraform Backend

```bash
cd terraform/modules/state-backend
terraform init
terraform apply
```

### 2. Deploy Infrastructure

```bash
cd terraform/environments/dev
terraform init
terraform plan
terraform apply
```

### 3. Deploy Observability Stack

```bash
kubectl apply -f kubernetes/namespaces/
kubectl apply -f kubernetes/elk-stack/
kubectl apply -f kubernetes/prometheus/
kubectl apply -f kubernetes/grafana/
```

### 4. Build and Deploy Sample Application

```bash
cd sample-microservice
mvn clean package
docker build -t sample-microservice:latest .
kubectl apply -f kubernetes/sample-app/
```

##  Observability Features

### Structured Logging (ELK Stack)
- JSON-formatted logs for machine parsing
- Correlation IDs for request tracing
- Centralized log aggregation in Elasticsearch
- Kibana dashboards for log analysis

### Metrics (Prometheus + Grafana)
- JVM metrics (heap, GC, threads)
- Custom application metrics
- Kubernetes cluster metrics
- Pre-built Grafana dashboards

### Distributed Tracing (OpenTelemetry)
- End-to-end request tracing
- Service dependency mapping
- Latency analysis

##  Security Features

- **Encrypted S3 Buckets** - AES-256 encryption at rest
- **Private Subnets** - Workloads isolated from public internet
- **Security Groups** - Principle of least privilege
- **IAM Roles** - Fine-grained access control
- **Container Scanning** - Trivy integration in CI/CD

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

---

**Built with ❤️ for Platform Engineering**

