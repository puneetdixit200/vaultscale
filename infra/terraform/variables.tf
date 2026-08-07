# infra/terraform/variables.tf
# Declaring variables here means main.tf never contains hardcoded secrets.
# Actual values go in terraform.tfvars (which you .gitignore — NEVER commit it).

variable "tenancy_ocid"        { type = string }
variable "user_ocid"           { type = string }
variable "fingerprint"         { type = string }
variable "private_key_path"    { type = string }
variable "region"              { type = string, default = "us-ashburn-1" }
variable "compartment_id"      { type = string }
variable "availability_domain" { type = string }
variable "subnet_id"           { type = string }
variable "vcn_id"              { type = string }
variable "ubuntu_image_id"     { type = string }
variable "ssh_public_key_path" { type = string, default = "~/.ssh/id_rsa.pub" }
