# Project Forge Architecture

## Overview

Project Forge is a Platform Engineering Blueprint that provides a "Golden Path" for building production-ready cloud-native applications. It implements a complete infrastructure and observability stack following enterprise best practices.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud                                       │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                         VPC (10.0.0.0/16)                                ││
│  │                                                                          ││
│  │  ┌──────────────────────────┐    ┌──────────────────────────┐          ││
│  │  │    Public Subnet AZ-A    │    │    Public Subnet AZ-B    │          ││
│  │  │    (10.0.1.0/24)         │    │    (10.0.2.0/24)         │          ││
│  │  │  ┌──────────────────┐    │    │  ┌──────────────────┐    │          ││
│  │  │  │   NAT Gateway    │    │    │  │   NAT Gateway    │    │          ││
│  │  │  └──────────────────┘    │    │  └──────────────────┘    │          ││
│  │  │  ┌──────────────────┐    │    │  ┌──────────────────┐    │          ││
│  │  │  │  Load Balancer   │    │    │  │  Load Balancer   │    │          ││
│  │  │  └──────────────────┘    │    │  └──────────────────┘    │          ││
│  │  └──────────────────────────┘    └──────────────────────────┘          ││
│  │                                                                          ││
│  │  ┌──────────────────────────┐    ┌──────────────────────────┐          ││
│  │  │   Private Subnet AZ-A   │    │   Private Subnet AZ-B    │          ││
│  │  │   (10.0.10.0/24)        │    │   (10.0.20.0/24)         │          ││
│  │  │                          │    │                          │          ││
│  │  │  ┌────────────────────────────────────────────────────┐ │          ││
│  │  │  │              Amazon EKS Cluster                    │ │          ││
│  │  │  │  ┌──────────────────┐  ┌──────────────────┐       │ │          ││
│  │  │  │  │   applications   │  │  observability   │       │ │          ││
│  │  │  │  │   namespace      │  │   namespace      │       │ │          ││
│  │  │  │  │                  │  │                  │       │ │          ││
│  │  │  │  │ ┌──────────────┐ │  │ ┌──────────────┐ │       │ │          ││
│  │  │  │  │ │ Microservice │ │  │ │ Prometheus   │ │       │ │          ││
│  │  │  │  │ │   + SDK      │─┼──┼▶│              │ │       │ │          ││
│  │  │  │  │ └──────────────┘ │  │ └──────────────┘ │       │ │          ││
│  │  │  │  │        │         │  │        │         │       │ │          ││
│  │  │  │  │        │         │  │ ┌──────▼───────┐ │       │ │          ││
│  │  │  │  │        │         │  │ │   Grafana    │ │       │ │          ││
│  │  │  │  │        │         │  │ └──────────────┘ │       │ │          ││
│  │  │  │  │        │         │  │                  │       │ │          ││
│  │  │  │  │        └─────────┼──┼▶┌──────────────┐ │       │ │          ││
│  │  │  │  │                  │  │ │  Logstash    │ │       │ │          ││
│  │  │  │  │                  │  │ └──────┬───────┘ │       │ │          ││
│  │  │  │  │                  │  │        │         │       │ │          ││
│  │  │  │  │                  │  │ ┌──────▼───────┐ │       │ │          ││
│  │  │  │  │                  │  │ │Elasticsearch │ │       │ │          ││
│  │  │  │  │                  │  │ └──────────────┘ │       │ │          ││
│  │  │  │  │                  │  │        │         │       │ │          ││
│  │  │  │  │                  │  │ ┌──────▼───────┐ │       │ │          ││
│  │  │  │  │                  │  │ │   Kibana     │ │       │ │          ││
│  │  │  │  │                  │  │ └──────────────┘ │       │ │          ││
│  │  │  │  └──────────────────┘  └──────────────────┘       │ │          ││
│  │  │  └────────────────────────────────────────────────────┘ │          ││
│  │  └──────────────────────────┘    └──────────────────────────┘          ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                                                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   S3 Bucket     │  │   DynamoDB      │  │   CloudWatch    │             │
│  │ (TF State)      │  │ (State Lock)    │  │   (Logs)        │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Infrastructure Layer (Terraform)

#### Networking Module
- **VPC**: Multi-AZ VPC with CIDR 10.0.0.0/16
- **Public Subnets**: For NAT Gateways and Load Balancers
- **Private Subnets**: For EKS nodes and workloads
- **NAT Gateways**: High availability with one per AZ (production)
- **VPC Flow Logs**: For network traffic auditing

#### EKS Cluster Module
- **Managed Node Groups**: Auto-scaling worker nodes
- **OIDC Provider**: For IAM Roles for Service Accounts (IRSA)
- **Secrets Encryption**: KMS-encrypted Kubernetes secrets
- **Control Plane Logging**: API, audit, authenticator logs

#### State Backend Module
- **S3 Bucket**: Versioned, encrypted state storage
- **DynamoDB Table**: State locking to prevent conflicts
- **KMS Key**: Customer-managed encryption key

### 2. Observability Layer

#### Logging (ELK Stack)
```
Application → Logstash → Elasticsearch → Kibana
     │
     └─ JSON logs with:
        - Correlation ID
        - Application name
        - Environment
        - Structured fields
```

#### Metrics (Prometheus + Grafana)
```
Application → /actuator/prometheus → Prometheus → Grafana
     │
     └─ Metrics include:
        - JVM metrics (heap, GC, threads)
        - HTTP request metrics
        - Custom business metrics
```

#### Tracing (OpenTelemetry)
```
Application → OTLP Exporter → Collector → Backend (Jaeger/Tempo)
     │
     └─ Trace context:
        - Trace ID
        - Span ID
        - Parent spans
        - Attributes
```

### 3. Application Layer

#### Observability SDK
The Java library that every microservice includes:
- **Structured Logging**: JSON logs compatible with ELK
- **Metrics**: Micrometer with Prometheus registry
- **Tracing**: OpenTelemetry with automatic instrumentation

#### Sample Microservice
Demonstrates SDK usage with:
- REST API with health endpoints
- Prometheus metrics endpoint
- Structured JSON logging
- Distributed tracing

### 4. CI/CD Pipeline

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│    Commit    │───▶│   CI Build   │───▶│  Container   │
│              │    │   & Test     │    │    Build     │
└──────────────┘    └──────────────┘    └──────────────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    ▼                          ▼                          ▼
             ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
             │     Dev      │─────────▶│   Staging    │─────────▶│ Production   │
             │   Deploy     │          │   Deploy     │          │   Deploy     │
             └──────────────┘          └──────────────┘          └──────────────┘
```

## Security Architecture

### Network Security
- Private subnets for workloads
- Security groups with least privilege
- VPC Flow Logs for auditing

### Data Security
- Encryption at rest (S3, EBS, secrets)
- Encryption in transit (TLS)
- KMS customer-managed keys

### Access Control
- IAM roles with minimal permissions
- IRSA for pod-level AWS access
- RBAC for Kubernetes access

### DevSecOps
- Container scanning (Trivy)
- Dependency scanning (OWASP)
- Infrastructure scanning (Checkov)
- Secret scanning (Gitleaks)

## Scalability

### Horizontal Pod Autoscaler
- CPU-based scaling (70% threshold)
- Memory-based scaling (80% threshold)
- Min/max replica configuration

### Cluster Autoscaler
- Automatic node scaling
- Spot instances for cost optimization
- Multi-AZ distribution

## Disaster Recovery

### Data Backup
- Terraform state versioned in S3
- Elasticsearch snapshots
- Prometheus persistent storage

### Multi-AZ Deployment
- Workloads spread across AZs
- NAT Gateways per AZ
- Pod disruption budgets

