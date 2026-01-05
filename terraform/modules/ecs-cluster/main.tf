################################################################################
# Project Forge - ECS Cluster Module
# Creates an Amazon ECS Fargate cluster with capacity providers
################################################################################

terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

################################################################################
# ECS Cluster
################################################################################

resource "aws_ecs_cluster" "main" {
  name = var.cluster_name

  setting {
    name  = "containerInsights"
    value = var.enable_container_insights ? "enabled" : "disabled"
  }

  configuration {
    execute_command_configuration {
      kms_key_id = var.kms_key_arn != "" ? var.kms_key_arn : aws_kms_key.ecs[0].arn
      logging    = "OVERRIDE"

      log_configuration {
        cloud_watch_encryption_enabled = true
        cloud_watch_log_group_name     = aws_cloudwatch_log_group.ecs_exec.name
      }
    }
  }

  tags = var.tags
}

################################################################################
# Capacity Providers
################################################################################

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = var.fargate_base_capacity
    weight            = var.fargate_weight
    capacity_provider = "FARGATE"
  }

  default_capacity_provider_strategy {
    weight            = var.fargate_spot_weight
    capacity_provider = "FARGATE_SPOT"
  }
}

################################################################################
# KMS Key for ECS
################################################################################

resource "aws_kms_key" "ecs" {
  count = var.kms_key_arn == "" ? 1 : 0

  description             = "KMS key for ECS cluster ${var.cluster_name}"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = var.tags
}

resource "aws_kms_alias" "ecs" {
  count = var.kms_key_arn == "" ? 1 : 0

  name          = "alias/${var.cluster_name}-ecs"
  target_key_id = aws_kms_key.ecs[0].key_id
}

################################################################################
# CloudWatch Log Groups
################################################################################

resource "aws_cloudwatch_log_group" "ecs_exec" {
  name              = "/aws/ecs/${var.cluster_name}/exec"
  retention_in_days = var.log_retention_days

  tags = var.tags
}

resource "aws_cloudwatch_log_group" "ecs_tasks" {
  name              = "/aws/ecs/${var.cluster_name}/tasks"
  retention_in_days = var.log_retention_days

  tags = var.tags
}

################################################################################
# ECS Task Execution Role
################################################################################

resource "aws_iam_role" "task_execution" {
  name = "${var.cluster_name}-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "task_execution" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  role       = aws_iam_role.task_execution.name
}

resource "aws_iam_role_policy" "task_execution_secrets" {
  name = "${var.cluster_name}-task-execution-secrets"
  role = aws_iam_role.task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "ssm:GetParameters"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = var.kms_key_arn != "" ? var.kms_key_arn : aws_kms_key.ecs[0].arn
      }
    ]
  })
}

################################################################################
# Default Task Role
################################################################################

resource "aws_iam_role" "task" {
  name = "${var.cluster_name}-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy" "task_cloudwatch" {
  name = "${var.cluster_name}-task-cloudwatch"
  role = aws_iam_role.task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "${aws_cloudwatch_log_group.ecs_tasks.arn}:*"
      }
    ]
  })
}

################################################################################
# Security Group for ECS Tasks
################################################################################

resource "aws_security_group" "ecs_tasks" {
  name        = "${var.cluster_name}-ecs-tasks-sg"
  description = "Security group for ECS tasks"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.cluster_name}-ecs-tasks-sg"
  })
}

################################################################################
# Service Discovery Namespace
################################################################################

resource "aws_service_discovery_private_dns_namespace" "main" {
  count = var.enable_service_discovery ? 1 : 0

  name        = "${var.cluster_name}.local"
  description = "Service discovery namespace for ${var.cluster_name}"
  vpc         = var.vpc_id

  tags = var.tags
}

