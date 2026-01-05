# Project Forge Security Guide

## Security Overview

Project Forge implements defense-in-depth security following AWS Well-Architected Framework and industry best practices for regulated industries like Finance and Defence.

## Network Security

### VPC Architecture
- **Private Subnets**: All workloads run in private subnets without direct internet access
- **NAT Gateways**: Outbound internet access controlled through NAT
- **VPC Flow Logs**: All network traffic logged for auditing

### Security Groups

```hcl
# Example: EKS Node Security Group
- Ingress from control plane only
- Ingress from other nodes (pod-to-pod)
- No direct public access
```

### Network Policies (Kubernetes)
```yaml
# Deny all by default
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
spec:
  podSelector: {}
  policyTypes:
    - Ingress
    - Egress
```

## Identity and Access Management

### AWS IAM
- **Principle of Least Privilege**: Minimal permissions for each role
- **Service-Linked Roles**: Specific roles for EKS, ECS, etc.
- **IAM Roles for Service Accounts (IRSA)**: Pod-level AWS permissions

### Kubernetes RBAC
```yaml
# Example: Read-only role for developers
kind: Role
metadata:
  name: developer
rules:
  - apiGroups: [""]
    resources: ["pods", "services", "configmaps"]
    verbs: ["get", "list", "watch"]
```

## Data Protection

### Encryption at Rest
| Resource | Encryption Method |
|----------|------------------|
| S3 Buckets | AES-256 / KMS |
| EBS Volumes | KMS |
| Kubernetes Secrets | KMS via EKS |
| DynamoDB | KMS |
| Elasticsearch | KMS |

### Encryption in Transit
- All internal communication uses TLS 1.2+
- Load balancer terminates external TLS
- Service mesh (optional) for mTLS

### Key Management
```hcl
# KMS key with rotation
resource "aws_kms_key" "main" {
  enable_key_rotation = true
  deletion_window_in_days = 30
}
```

## Container Security

### Image Security
1. **Base Images**: Use minimal, distroless images
2. **Vulnerability Scanning**: Trivy in CI/CD pipeline
3. **Image Signing**: (Optional) Cosign/Notary

### Runtime Security
```dockerfile
# Non-root user
USER 1001:1001

# Read-only filesystem
RUN chmod -R 555 /app
```

### Pod Security
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
```

## Application Security

### Logging Security
- No PII in logs
- Correlation IDs for request tracing
- Centralized log aggregation
- Log retention policies

### Secrets Management
1. **Kubernetes Secrets**: For non-sensitive config
2. **AWS Secrets Manager**: For credentials
3. **External Secrets Operator**: Sync to Kubernetes

### Input Validation
- Validate all user input
- Use parameterized queries
- Sanitize output

## DevSecOps Pipeline

### Security Scanning
```yaml
# CI Pipeline Security Gates
- Dependency Scan (OWASP)
- Container Scan (Trivy)
- Infrastructure Scan (Checkov)
- Secret Scan (Gitleaks)
- SAST (SonarQube) - optional
```

### Vulnerability Thresholds
| Severity | Action |
|----------|--------|
| Critical | Block deployment |
| High | Block deployment |
| Medium | Warning |
| Low | Informational |

## Compliance Considerations

### For Financial Services
- **Audit Logging**: All actions logged with timestamps
- **Data Encryption**: Encryption everywhere
- **Access Controls**: RBAC with audit trail
- **Change Management**: GitOps with approval workflows

### For Defence
- **Network Isolation**: Private subnets, no public endpoints
- **Security Clearance**: IAM with MFA
- **Data Classification**: Labels and policies
- **Incident Response**: Alerting and runbooks

## Security Checklist

### Infrastructure
- [ ] VPC with private subnets
- [ ] NAT Gateway for outbound traffic
- [ ] VPC Flow Logs enabled
- [ ] Security groups with least privilege
- [ ] KMS encryption enabled
- [ ] IAM roles with minimal permissions

### Kubernetes
- [ ] RBAC configured
- [ ] Pod security policies/standards
- [ ] Network policies
- [ ] Secrets encrypted
- [ ] Service accounts with IRSA

### Application
- [ ] Non-root container
- [ ] Read-only filesystem
- [ ] No hardcoded secrets
- [ ] Input validation
- [ ] Structured logging

### Pipeline
- [ ] Container scanning
- [ ] Dependency scanning
- [ ] Secret scanning
- [ ] Infrastructure scanning
- [ ] Signed commits

## Incident Response

### Monitoring
- Prometheus alerts for anomalies
- CloudWatch alarms for AWS resources
- Kibana dashboards for logs

### Response Procedures
1. **Detection**: Automated alerting
2. **Containment**: Network isolation
3. **Eradication**: Remediation
4. **Recovery**: Restore from backup
5. **Lessons Learned**: Post-mortem

## Security Updates

### Patching Schedule
- **Critical**: Within 24 hours
- **High**: Within 7 days
- **Medium**: Within 30 days
- **Low**: Next release cycle

### Update Process
1. Security advisory review
2. Impact assessment
3. Test in staging
4. Deploy to production
5. Verify remediation

