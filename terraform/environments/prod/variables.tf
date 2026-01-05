################################################################################
# Project Forge - Production Environment Variables
################################################################################

variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "project-forge"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.1.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones (3 for production HA)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "kubernetes_version" {
  description = "Kubernetes version"
  type        = string
  default     = "1.29"
}

variable "enable_public_access" {
  description = "Enable public API access"
  type        = bool
  default     = false
}

variable "public_access_cidrs" {
  description = "CIDR blocks for public API access"
  type        = list(string)
  default     = []
}

variable "node_instance_types" {
  description = "Instance types for on-demand node group"
  type        = list(string)
  default     = ["m5.large", "m5.xlarge"]
}

variable "node_desired_size" {
  description = "Desired number of on-demand nodes"
  type        = number
  default     = 3
}

variable "node_min_size" {
  description = "Minimum number of on-demand nodes"
  type        = number
  default     = 3
}

variable "node_max_size" {
  description = "Maximum number of on-demand nodes"
  type        = number
  default     = 10
}

variable "spot_instance_types" {
  description = "Instance types for spot node group"
  type        = list(string)
  default     = ["m5.large", "m5.xlarge", "m4.large", "m4.xlarge"]
}

variable "spot_desired_size" {
  description = "Desired number of spot nodes"
  type        = number
  default     = 2
}

variable "spot_max_size" {
  description = "Maximum number of spot nodes"
  type        = number
  default     = 10
}

