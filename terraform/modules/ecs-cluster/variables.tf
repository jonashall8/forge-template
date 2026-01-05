################################################################################
# Project Forge - ECS Cluster Module Variables
################################################################################

variable "cluster_name" {
  description = "Name of the ECS cluster"
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "enable_container_insights" {
  description = "Enable CloudWatch Container Insights"
  type        = bool
  default     = true
}

variable "kms_key_arn" {
  description = "ARN of the KMS key for encryption (creates new if empty)"
  type        = string
  default     = ""
}

variable "log_retention_days" {
  description = "Number of days to retain logs"
  type        = number
  default     = 30
}

variable "fargate_base_capacity" {
  description = "Base capacity for Fargate provider"
  type        = number
  default     = 1
}

variable "fargate_weight" {
  description = "Weight for Fargate provider"
  type        = number
  default     = 1
}

variable "fargate_spot_weight" {
  description = "Weight for Fargate Spot provider"
  type        = number
  default     = 1
}

variable "enable_service_discovery" {
  description = "Enable AWS Cloud Map service discovery"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}

