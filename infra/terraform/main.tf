# Provisions one Oracle Cloud Ampere A1 VM for a VaultScale demo/staging deployment.
# Current OCI Always Free guidance equates the A1 allowance to 2 OCPUs / 12 GB
# total for Always Free tenancies. Keep this file within that envelope.

terraform {
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 5.0"
    }
  }
}

provider "oci" {
  tenancy_ocid     = var.tenancy_ocid
  user_ocid        = var.user_ocid
  fingerprint      = var.fingerprint
  private_key_path = var.private_key_path
  region           = var.region
}

resource "oci_core_network_security_group" "vaultscale" {
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id
  display_name   = "vaultscale-nsg"
}

resource "oci_core_network_security_group_security_rule" "http" {
  network_security_group_id = oci_core_network_security_group.vaultscale.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"

  tcp_options {
    destination_port_range {
      min = 80
      max = 80
    }
  }
}

resource "oci_core_network_security_group_security_rule" "ssh" {
  network_security_group_id = oci_core_network_security_group.vaultscale.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = var.ssh_allowed_cidr
  source_type               = "CIDR_BLOCK"

  tcp_options {
    destination_port_range {
      min = 22
      max = 22
    }
  }
}

resource "oci_core_instance" "vaultscale_vm" {
  compartment_id      = var.compartment_id
  availability_domain = var.availability_domain
  shape                = "VM.Standard.A1.Flex"

  shape_config {
    ocpus         = 2
    memory_in_gbs = 12
  }

  source_details {
    source_type = "image"
    source_id   = var.ubuntu_image_id
  }

  create_vnic_details {
    subnet_id        = var.subnet_id
    assign_public_ip = true
    nsg_ids          = [oci_core_network_security_group.vaultscale.id]
  }

  metadata = {
    ssh_authorized_keys = file(var.ssh_public_key_path)
  }

  display_name = "vaultscale-server"
}

output "public_ip" {
  value = oci_core_instance.vaultscale_vm.public_ip
}
