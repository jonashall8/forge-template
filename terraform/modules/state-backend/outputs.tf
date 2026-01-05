################################################################################
# Project Forge - State Backend Module Outputs
################################################################################

output "s3_bucket_id" {
  description = "The name of the S3 bucket"
  value       = aws_s3_bucket.terraform_state.id
}

output "s3_bucket_arn" {
  description = "The ARN of the S3 bucket"
  value       = aws_s3_bucket.terraform_state.arn
}

output "s3_bucket_region" {
  description = "The region of the S3 bucket"
  value       = aws_s3_bucket.terraform_state.region
}

output "dynamodb_table_name" {
  description = "The name of the DynamoDB table"
  value       = aws_dynamodb_table.terraform_locks.name
}

output "dynamodb_table_arn" {
  description = "The ARN of the DynamoDB table"
  value       = aws_dynamodb_table.terraform_locks.arn
}

output "kms_key_arn" {
  description = "The ARN of the KMS key (if enabled)"
  value       = var.use_kms_encryption ? aws_kms_key.terraform_state[0].arn : null
}

output "kms_key_id" {
  description = "The ID of the KMS key (if enabled)"
  value       = var.use_kms_encryption ? aws_kms_key.terraform_state[0].key_id : null
}

output "backend_config" {
  description = "Backend configuration for use in other Terraform projects"
  value = {
    bucket         = aws_s3_bucket.terraform_state.id
    dynamodb_table = aws_dynamodb_table.terraform_locks.name
    region         = aws_s3_bucket.terraform_state.region
    encrypt        = true
    kms_key_id     = var.use_kms_encryption ? aws_kms_key.terraform_state[0].arn : null
  }
}

