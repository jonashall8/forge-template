# Getting Started with Project Forge

This guide will help you set up and run Project Forge locally and deploy it to AWS.

## Prerequisites

### Required Tools
- **Docker** (v24.0+) and **Docker Compose** (v2.0+)
- **Java 17+** and **Maven 3.9+**
- **Terraform** (v1.5+)
- **kubectl** (v1.28+)
- **AWS CLI** (v2.0+)
- **Helm** (v3.13+)

### AWS Setup
1. Configure AWS credentials:
   ```bash
   aws configure
   ```
2. Ensure you have permissions for: VPC, EKS, ECS, S3, DynamoDB, IAM, ECR

## Quick Start (Local Development)

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/project-forge.git
cd project-forge
```

### 2. Run Setup Script
```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

This will:
- Build the Java Observability SDK
- Build the sample microservice
- Start the observability stack (Elasticsearch, Logstash, Kibana, Prometheus, Grafana)

### 3. Access Services
| Service | URL | Credentials |
|---------|-----|-------------|
| Sample API | http://localhost:8080/api/orders | - |
| Health Check | http://localhost:8080/actuator/health | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin |
| Kibana | http://localhost:5601 | - |
| Elasticsearch | http://localhost:9200 | - |

### 4. Test the Application
```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "cust-123"}'

# List orders
curl http://localhost:8080/api/orders

# View metrics
curl http://localhost:8080/actuator/prometheus
```

## AWS Deployment

### 1. Initialize Terraform State Backend
```bash
cd terraform/modules/state-backend
terraform init
terraform apply -var="bucket_name=your-project-forge-state" \
                -var="dynamodb_table_name=your-project-forge-locks"
```

### 2. Deploy Development Environment
```bash
cd terraform/environments/dev
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values

terraform init
terraform plan
terraform apply
```

### 3. Deploy Observability Stack
```bash
# Update kubeconfig
aws eks update-kubeconfig --name project-forge-dev --region us-east-1

# Deploy namespaces and observability
kubectl apply -f kubernetes/namespaces/
kubectl apply -f kubernetes/elk-stack/
kubectl apply -f kubernetes/prometheus/
kubectl apply -f kubernetes/grafana/
```

### 4. Deploy Sample Application
```bash
# Build and push Docker image
cd sample-microservice
docker build -t your-registry/sample-microservice:latest .
docker push your-registry/sample-microservice:latest

# Update image in deployment and apply
kubectl apply -f kubernetes/sample-app/
```

## Using the Observability SDK

### Add Dependency
```xml
<dependency>
    <groupId>com.projectforge</groupId>
    <artifactId>observability-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configure Application
```yaml
projectforge:
  observability:
    enabled: true
    application-name: your-service
    environment: dev
    logging:
      enabled: true
    metrics:
      enabled: true
    tracing:
      enabled: true
      exporter-endpoint: http://otel-collector:4317
```

### Use Structured Logging
```java
import com.projectforge.observability.logging.StructuredLogger;

StructuredLogger logger = StructuredLogger.getLogger(MyService.class);

logger.withField("orderId", orderId)
      .withField("customerId", customerId)
      .info("Order processed");
```

### Use Custom Metrics
```java
@Autowired
private CustomMetricsService metricsService;

metricsService.incrementCounter("orders.created", "status", "success");
metricsService.recordTimer("order.processing", duration, TimeUnit.MILLISECONDS);
```

### Use Tracing
```java
@Autowired
private SpanService spanService;

return spanService.withSpan("processOrder", span -> {
    span.setAttribute("orderId", orderId);
    // ... process order
    return result;
});
```

## Stopping Services

### Local
```bash
cd docker
docker-compose down
```

### AWS
```bash
./scripts/cleanup.sh dev
```

## Next Steps

- [Architecture Overview](ARCHITECTURE.md)
- [Security Guide](SECURITY.md)
- [Contributing Guidelines](../CONTRIBUTING.md)

