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

resource "azurerm_resource_group" "ageramvd" {
  name     = var.resource_group
  location = "West Europe"
}

resource "azurerm_container_group" "edc" {
  name                = "${var.prefix}-edc"
  location            = var.location
  resource_group_name = azurerm_resource_group.ageramvd.name
  ip_address_type     = "public"
  dns_name_label      = "${var.prefix}-agera-mvd"
  os_type             = "Linux"

  container {
    name   = "${var.prefix}-edc"
    image  = "${var.repository}:${var.image_tag}"
    cpu    = "0.5"
    memory = "1.5"

    ports {
      port     = 80
      protocol = "TCP"
    }
  }
}

