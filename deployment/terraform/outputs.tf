output "edc_host" {
  value = azurerm_container_group.edc.fqdn
}

output "assets_storage_account" {
  value = azurerm_storage_account.assets.name
}

output "did_endpoint" {
  value = "${azurerm_storage_account.did.primary_web_endpoint}${azurerm_storage_blob.did.name}"
}