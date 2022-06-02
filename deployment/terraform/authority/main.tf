terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.1.0"
    }
  }

  backend "azurerm" {}
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy = true
    }
  }
}

data "azurerm_subscription" "current_subscription" {
}

data "azurerm_client_config" "current_client" {
}

resource "azurerm_resource_group" "authority" {
  name     = var.resource_group
  location = var.location
}

resource "azurerm_application_insights" "authority" {
  name                = "${var.prefix}-appinsights"
  location            = var.location
  resource_group_name = azurerm_resource_group.authority.name
  application_type    = "java"
}

