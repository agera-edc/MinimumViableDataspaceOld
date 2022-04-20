variable "prefix" {
  description = "Prefix of resource names. Guarantee uniqueness of resource names to be able to deploy several MVD without conflicts."
  default     = "test"
}

variable "participant_name" {
  default = "testparticipant"
}

variable "runtime_image" {
}

variable "location" {
  default = "northeurope"
}

variable "acr_name" {
  default = "ageramvd"
}

variable "common_resource_group" {
  default = "agera-mvd-common"
}

variable "terraform_state_storage_account" {
  default = "mvdterraformstates"
}

variable "terraform_state_container" {
  default = "mvdterraformstates"
}

variable "resource_group" {
  default = "test-resource-group"
}

variable "container_cpu" {
  default = "0.5"
}

variable "container_memory" {
  default = "1.5"
}

variable "application_sp_object_id" {
  description = "id of application's service principal object"
}

variable "key_file" {
  description = "name of a file containing the private key in PEM format"
  default = "../../key.pem"
}
