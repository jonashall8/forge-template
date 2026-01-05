################################################################################
# Project Forge - State Backend Module Variables
################################################################################

variable "bucket_name" {
  description = "Name of the S3 bucket for Terraform state"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table for state locking"
  type        = string
}

variable "use_kms_encryption" {
  description = "Use KMS encryption for S3 bucket and DynamoDB"
  type        = bool
  default     = true
}

variable "noncurrent_version_expiration_days" {
  description = "Number of days to retain noncurrent versions"
  type        = number
  default     = 90
}

variable "enable_point_in_time_recovery" {
  description = "Enable point-in-time recovery for DynamoDB"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}

