# infra/terraform/main.tf
# Provisions a free-tier VM on Oracle Cloud to run VaultScale via Docker Compose.

terraform {
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 5.0"
    }
  }
}

# Configure connection to your Oracle Cloud account
# (values come from variables.tf — never hardcode secrets directly here)
provider "oci" {
  tenancy_ocid     = var.tenancy_ocid
  user_ocid        = var.user_ocid
  fingerprint      = var.fingerprint
  private_key_path = var.private_key_path
  region           = var.region
}

# ─── The Always-Free VM instance ─────────────────────────────────────────
resource "oci_core_instance" "vaultscale_vm" {
  compartment_id      = var.compartment_id
  availability_domain = var.availability_domain
  shape                = "VM.Standard.A1.Flex"   # Oracle's Always Free ARM shape

  shape_config {
    ocpus         = 4     # max free allowance
    memory_in_gbs = 24    # max free allowance
  }

  source_details {
    source_type = "image"
    source_id   = var.ubuntu_image_id
  }

  create_vnic_details {
    subnet_id        = var.subnet_id
    assign_public_ip = true   # needed so we can SSH in and reach it publicly
  }

  metadata = {
    ssh_authorized_keys = file(var.ssh_public_key_path)
  }

  display_name = "vaultscale-server"
}

# ─── Firewall rule: allow inbound HTTP (port 80) and SSH (port 22) ───────
resource "oci_core_security_list" "vaultscale_sl" {
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id

  ingress_security_rules {
    protocol = "6"   # TCP
    source   = "0.0.0.0/0"
    tcp_options { min = 80; max = 80 }
  }

  ingress_security_rules {
    protocol = "6"
    source   = "0.0.0.0/0"
    tcp_options { min = 22; max = 22 }
  }
}

# ─── Output the public IP so we know where to SSH/browse after apply ────
output "public_ip" {
  value = oci_core_instance.vaultscale_vm.public_ip
}
