terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.1.0"
    }
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = true
      recover_soft_deleted_key_vaults = false
    }
  }
}

data "azurerm_subscription" "current_subscription" {
}

data "azurerm_client_config" "current_client" {
}

resource "azurerm_resource_group" "participant" {
  name     = var.resource_group
  location = var.location
}

data "azurerm_container_registry" "registry" {
  name                = var.acr_name
  resource_group_name = var.acr_resource_group
}

resource "azurerm_container_group" "edc" {
  name                = "${var.prefix}-${var.participant_name}-edc"
  location            = var.location
  resource_group_name = azurerm_resource_group.participant.name
  ip_address_type     = "Public"
  dns_name_label      = "${var.prefix}-${var.participant_name}-edc-mvd"
  os_type             = "Linux"

  image_registry_credential {
    username = data.azurerm_container_registry.registry.admin_username
    password = data.azurerm_container_registry.registry.admin_password
    server   = data.azurerm_container_registry.registry.login_server
  }

  container {
    name   = "${var.prefix}-${var.participant_name}-edc"
    image  = "${data.azurerm_container_registry.registry.login_server}/${var.runtime_image}"
    cpu    = var.container_cpu
    memory = var.container_memory

    ports {
      port     = 8181
      protocol = "TCP"
    }
    ports {
      port     = 9191
      protocol = "TCP"
    }
  }
}

resource "azurerm_key_vault" "participant" {
  name                        = "${var.prefix}${var.participant_name}"
  location                    = azurerm_resource_group.participant.location
  resource_group_name         = azurerm_resource_group.participant.name
  enabled_for_disk_encryption = false
  tenant_id                   = data.azurerm_client_config.current_client.tenant_id
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false
  sku_name                    = "standard"
  enable_rbac_authorization   = true
}

# Role assignment so that the application may access the vault
resource "azurerm_role_assignment" "edc_keyvault" {
  scope                = azurerm_key_vault.participant.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = var.application_object_id
}

# Role assignment so that the currently logged in user may add secrets to the vault
resource "azurerm_role_assignment" "current-user-secretsofficer" {
  scope                = azurerm_key_vault.participant.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current_client.object_id
}

resource "azurerm_storage_account" "assets" {
  name                     = "${var.prefix}${var.participant_name}"
  resource_group_name      = azurerm_resource_group.participant.name
  location                 = azurerm_resource_group.participant.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"

  static_website {
    index_document = "index.html"
  }
}

resource "azurerm_key_vault_secret" "asset_storage_account" {
  name         = "${azurerm_storage_account.assets.name}-key1"
  value        = azurerm_storage_account.assets.primary_access_key
  key_vault_id = azurerm_key_vault.participant.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}