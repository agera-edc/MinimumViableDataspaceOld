output "app_insights_connection_string" {
  value     = azurerm_application_insights.dataspace.connection_string
  sensitive = true
}

output "registration_service_url" {
  value = "http://${azurerm_container_group.registration-service.fqdn}:${local.edc_default_port}"
}