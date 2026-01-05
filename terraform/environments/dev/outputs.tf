################################################################################
# Project Forge - Development Environment Outputs
################################################################################

output "vpc_id" {
  description = "The ID of the VPC"
  value       = module.networking.vpc_id
}

output "private_subnet_ids" {
  description = "List of private subnet IDs"
  value       = module.networking.private_subnet_ids
}

output "public_subnet_ids" {
  description = "List of public subnet IDs"
  value       = module.networking.public_subnet_ids
}

output "eks_cluster_name" {
  description = "Name of the EKS cluster"
  value       = module.eks_cluster.cluster_name
}

output "eks_cluster_endpoint" {
  description = "Endpoint for the EKS cluster"
  value       = module.eks_cluster.cluster_endpoint
}

output "eks_cluster_certificate_authority" {
  description = "Certificate authority data for the EKS cluster"
  value       = module.eks_cluster.cluster_certificate_authority_data
  sensitive   = true
}

output "eks_oidc_provider_arn" {
  description = "ARN of the OIDC provider"
  value       = module.eks_cluster.oidc_provider_arn
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster (if deployed)"
  value       = var.deploy_ecs ? module.ecs_cluster[0].cluster_name : null
}

output "kubeconfig_command" {
  description = "Command to update kubeconfig"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks_cluster.cluster_name}"
}

