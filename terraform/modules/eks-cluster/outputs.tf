################################################################################
# Project Forge - EKS Cluster Module Outputs
################################################################################

output "cluster_id" {
  description = "The ID of the EKS cluster"
  value       = aws_eks_cluster.main.id
}

output "cluster_name" {
  description = "The name of the EKS cluster"
  value       = aws_eks_cluster.main.name
}

output "cluster_arn" {
  description = "The ARN of the EKS cluster"
  value       = aws_eks_cluster.main.arn
}

output "cluster_endpoint" {
  description = "The endpoint for the EKS cluster API server"
  value       = aws_eks_cluster.main.endpoint
}

output "cluster_version" {
  description = "The Kubernetes version of the cluster"
  value       = aws_eks_cluster.main.version
}

output "cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data for the cluster"
  value       = aws_eks_cluster.main.certificate_authority[0].data
}

output "cluster_security_group_id" {
  description = "Security group ID attached to the EKS cluster"
  value       = aws_security_group.cluster.id
}

output "cluster_iam_role_arn" {
  description = "IAM role ARN of the EKS cluster"
  value       = aws_iam_role.cluster.arn
}

output "node_group_role_arn" {
  description = "IAM role ARN of the EKS node groups"
  value       = aws_iam_role.node_group.arn
}

output "oidc_provider_arn" {
  description = "ARN of the OIDC Provider for IRSA"
  value       = aws_iam_openid_connect_provider.cluster.arn
}

output "oidc_provider_url" {
  description = "URL of the OIDC Provider"
  value       = aws_eks_cluster.main.identity[0].oidc[0].issuer
}

output "cluster_primary_security_group_id" {
  description = "The cluster primary security group ID"
  value       = aws_eks_cluster.main.vpc_config[0].cluster_security_group_id
}

output "kms_key_arn" {
  description = "ARN of the KMS key used for secrets encryption"
  value       = var.kms_key_arn != "" ? var.kms_key_arn : aws_kms_key.eks[0].arn
}

output "node_groups" {
  description = "Map of node group attributes"
  value = {
    for k, v in aws_eks_node_group.main : k => {
      arn    = v.arn
      status = v.status
    }
  }
}

