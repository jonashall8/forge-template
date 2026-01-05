################################################################################
# Project Forge - Development Environment
################################################################################

terraform {
  required_version = ">= 1.5.0"

  backend "s3" {
    bucket         = "project-forge-terraform-state"
    key            = "environments/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "project-forge-terraform-locks"
    encrypt        = true
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

################################################################################
# Local Variables
################################################################################

locals {
  cluster_name = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

################################################################################
# Networking
################################################################################

module "networking" {
  source = "../../modules/networking"

  project_name       = var.project_name
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  cluster_name       = local.cluster_name
  enable_nat_gateway = var.enable_nat_gateway
  single_nat_gateway = var.single_nat_gateway
  enable_flow_logs   = var.enable_flow_logs

  tags = local.common_tags
}

################################################################################
# EKS Cluster
################################################################################

module "eks_cluster" {
  source = "../../modules/eks-cluster"

  cluster_name              = local.cluster_name
  kubernetes_version        = var.kubernetes_version
  vpc_id                    = module.networking.vpc_id
  subnet_ids                = module.networking.private_subnet_ids
  endpoint_private_access   = true
  endpoint_public_access    = true
  public_access_cidrs       = var.public_access_cidrs
  enabled_cluster_log_types = ["api", "audit", "authenticator"]

  node_groups = {
    general = {
      instance_types = var.node_instance_types
      capacity_type  = "ON_DEMAND"
      disk_size      = 50
      desired_size   = var.node_desired_size
      min_size       = var.node_min_size
      max_size       = var.node_max_size
      labels = {
        role = "general"
      }
      taints = []
    }
  }

  tags = local.common_tags
}

################################################################################
# ECS Cluster (Optional)
################################################################################

module "ecs_cluster" {
  source = "../../modules/ecs-cluster"
  count  = var.deploy_ecs ? 1 : 0

  cluster_name              = "${local.cluster_name}-ecs"
  vpc_id                    = module.networking.vpc_id
  enable_container_insights = true
  enable_service_discovery  = true

  tags = local.common_tags
}

