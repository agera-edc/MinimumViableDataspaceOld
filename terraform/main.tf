terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.85.0"
    }
  }
}

provider "azurerm" {
  features {}
}

locals {
  deploy_resource_group = "${var.prefix}-agera-mvd"
}

resource "azurerm_resource_group" "ageramvd" {
  name     = local.deploy_resource_group
  location = "West Europe"
}

data "azurerm_container_registry" "registry" {
  name                = var.registry
  resource_group_name = var.registry_resource_group
}

resource "azurerm_container_group" "edc" {
  name                = "${var.prefix}-edc"
  location            = var.location
  resource_group_name = local.deploy_resource_group
  ip_address_type     = "public"
  dns_name_label      = "${var.prefix}-agera-mvd"
  os_type             = "Linux"

  image_registry_credential {
    username = data.azurerm_container_registry.registry.admin_username
    password = data.azurerm_container_registry.registry.admin_password
    server   = data.azurerm_container_registry.registry.login_server
  }

  container {
    name   = "${var.prefix}-${var.participant_name}-edc"
    image  = var.image
    cpu    = "0.5"
    memory = "1.5"

    ports {
      port     = 80
      protocol = "TCP"
    }
  }
}
