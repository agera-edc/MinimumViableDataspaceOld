output "app_insights_connection_string" {
  value     = azurerm_application_insights.authority.connection_string
  sensitive = true
}
