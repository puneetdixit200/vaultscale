# OCI account and deployment inputs. Put concrete values in terraform.tfvars,
# which is gitignored and must never be committed.

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

variable "ssh_allowed_cidr" {
  type        = string
  description = "CIDR allowed to SSH to the VM, for example your public IP as 203.0.113.10/32. Do not use 0.0.0.0/0 for a real deployment."
}
