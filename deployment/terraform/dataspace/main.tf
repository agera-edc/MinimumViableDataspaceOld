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
    // When deleting App Insights, resources related to microsoft.alertsmanagement remain
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

data "azurerm_subscription" "current_subscription" {
}

data "azurerm_client_config" "current_client" {
}

data "azurerm_container_registry" "registry" {
  name                = var.acr_name
  resource_group_name = var.acr_resource_group
}

data "azurerm_storage_account" "registry" {
  name                = var.registry_storage_account
  resource_group_name = var.registry_resource_group
}

data "azurerm_storage_share" "registry" {
  name                 = var.registry_share
  storage_account_name = data.azurerm_storage_account.registry.name
}

locals {
  registry_files_prefix = "${var.prefix}-"

  connector_name = "connector-registry"

  did_url = "did:web:${azurerm_storage_account.did.primary_web_host}"

  registry_service_dns_label = "${var.prefix}-registry-mvd"
  edc_default_port           = 8181
}

resource "azurerm_resource_group" "dataspace" {
  name     = var.resource_group
  location = var.location
}

resource "azurerm_application_insights" "dataspace" {
  name                = "${var.prefix}-appinsights"
  location            = var.location
  resource_group_name = azurerm_resource_group.dataspace.name
  application_type    = "java"
}

resource "azurerm_container_group" "registry-service" {
  name                = "${var.prefix}-registry"
  location            = var.location
  resource_group_name = azurerm_resource_group.dataspace.name
  ip_address_type     = "Public"
  dns_name_label      = local.registry_service_dns_label
  os_type             = "Linux"

  image_registry_credential {
    username = data.azurerm_container_registry.registry.admin_username
    password = data.azurerm_container_registry.registry.admin_password
    server   = data.azurerm_container_registry.registry.login_server
  }

  container {
    name   = "registry-service"
    image  = "${data.azurerm_container_registry.registry.login_server}/${var.registry_runtime_image}"
    cpu    = var.container_cpu
    memory = var.container_memory

    ports {
      port     = local.edc_default_port
      protocol = "TCP"
    }

    environment_variables = {
      EDC_CONNECTOR_NAME = local.connector_name

      NODES_JSON_DIR          = "/registry"
      NODES_JSON_FILES_PREFIX = local.registry_files_prefix
    }

    volume {
      storage_account_name = data.azurerm_storage_account.registry.name
      storage_account_key  = data.azurerm_storage_account.registry.primary_access_key
      share_name           = data.azurerm_storage_share.registry.name
      mount_path           = "/registry"
      name                 = "registry"
    }

    liveness_probe {
      http_get {
        port = 8181
        path = "/api/check/health"
      }
    }
  }
}

resource "azurerm_key_vault" "registry" {
  // added `kv` prefix because the keyvault name needs to begin with a letter
  name                        = "kv${var.prefix}-registry"
  location                    = var.location
  resource_group_name         = azurerm_resource_group.dataspace.name
  enabled_for_disk_encryption = false
  tenant_id                   = data.azurerm_client_config.current_client.tenant_id
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false
  sku_name                    = "standard"
  enable_rbac_authorization   = true
}

# Role assignment so that the application may access the vault
resource "azurerm_role_assignment" "registry_keyvault" {
  scope                = azurerm_key_vault.registry.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = var.application_sp_object_id
}

# Role assignment so that the currently logged in user may add secrets to the vault
resource "azurerm_role_assignment" "current-user-secretsofficer" {
  scope                = azurerm_key_vault.registry.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current_client.object_id
}

resource "azurerm_storage_account" "did" {
  name                     = "${var.prefix}did"
  resource_group_name      = azurerm_resource_group.dataspace.name
  location                 = var.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
  static_website {}
}

resource "azurerm_key_vault_secret" "did_key" {
  name = local.connector_name
  # Create did_key secret only if key_file value is provided. Default key_file value is null.
  count        = var.key_file == null ? 0 : 1
  value        = file(var.key_file)
  key_vault_id = azurerm_key_vault.registry.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_storage_blob" "did" {
  name                 = ".well-known/did.json" # `.well-known` path is defined by did:web specification
  storage_account_name = azurerm_storage_account.did.name
  # Create did blob only if public_key_jwk_file is provided. Default public_key_jwk_file value is null.
  count                  = var.public_key_jwk_file == null ? 0 : 1
  storage_container_name = "$web" # container used to serve static files (see static_website property on storage account)
  type                   = "Block"
  source_content = jsonencode({
    id = local.did_url
    "@context" = [
      "https://www.w3.org/ns/did/v1",
      {
        "@base" = local.did_url
      }
    ],
    "verificationMethod" = [
      {
        "id"           = "#identity-key-1"
        "controller"   = ""
        "type"         = "JsonWebKey2020"
        "publicKeyJwk" = jsondecode(file(var.public_key_jwk_file))
      }
    ],
    "authentication" : [
      "#identity-key-1"
  ] })
  content_type = "application/json"
}

