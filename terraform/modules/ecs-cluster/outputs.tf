################################################################################
# Project Forge - ECS Cluster Module Outputs
################################################################################

output "cluster_id" {
  description = "The ID of the ECS cluster"
  value       = aws_ecs_cluster.main.id
}

output "cluster_name" {
  description = "The name of the ECS cluster"
  value       = aws_ecs_cluster.main.name
}

output "cluster_arn" {
  description = "The ARN of the ECS cluster"
  value       = aws_ecs_cluster.main.arn
}

output "task_execution_role_arn" {
  description = "ARN of the task execution IAM role"
  value       = aws_iam_role.task_execution.arn
}

output "task_execution_role_name" {
  description = "Name of the task execution IAM role"
  value       = aws_iam_role.task_execution.name
}

output "task_role_arn" {
  description = "ARN of the default task IAM role"
  value       = aws_iam_role.task.arn
}

output "task_role_name" {
  description = "Name of the default task IAM role"
  value       = aws_iam_role.task.name
}

output "ecs_tasks_security_group_id" {
  description = "Security group ID for ECS tasks"
  value       = aws_security_group.ecs_tasks.id
}

output "cloudwatch_log_group_name" {
  description = "Name of the CloudWatch Log Group for ECS tasks"
  value       = aws_cloudwatch_log_group.ecs_tasks.name
}

output "cloudwatch_log_group_arn" {
  description = "ARN of the CloudWatch Log Group for ECS tasks"
  value       = aws_cloudwatch_log_group.ecs_tasks.arn
}

output "service_discovery_namespace_id" {
  description = "ID of the service discovery namespace"
  value       = var.enable_service_discovery ? aws_service_discovery_private_dns_namespace.main[0].id : null
}

output "service_discovery_namespace_arn" {
  description = "ARN of the service discovery namespace"
  value       = var.enable_service_discovery ? aws_service_discovery_private_dns_namespace.main[0].arn : null
}

output "kms_key_arn" {
  description = "ARN of the KMS key used for encryption"
  value       = var.kms_key_arn != "" ? var.kms_key_arn : aws_kms_key.ecs[0].arn
}

