variable "prefix" {
  description = "Prefix of resource names. Guarantee uniqueness of resource names to be able to deploy several MVD without conflicts."
  default     = "test"
}

variable "location" {
  default = "northeurope"
}

variable "resource_group" {
  default = "test-dataspace"
}

variable "runtime_image" {
  description = "Image name of the EDC Connector to deploy"
}

variable "acr_name" {
  default = "ageramvd"
}

variable "acr_resource_group" {
  default = "agera-mvd-common"
}

variable "container_cpu" {
  default = "0.5"
}

variable "container_memory" {
  default = "8"
}

variable "registry_resource_group" {
  description = "resource group of the registry JSON documents file share storage account"
}

variable "registry_storage_account" {
  description = "name of the registry JSON documents file share storage account"
}

variable "registry_share" {
  description = "name of the registry JSON documents file share"
}

