#!/bin/bash

set -euo pipefail

cd $(dirname "$0")

. .env

echo "== Checking Azure CLI login =="

if ! subscription_id=$(az account show --query id -o tsv)
then
  echo "Azure CLI not configured"
  echo "Please sign in with Azure CLI: https://docs.microsoft.com/cli/azure/authenticate-azure-cli"
  exit 1
fi

tenant_id=$(az account show --query tenantId -o tsv)

echo "== Checking GitHub contributor permissions =="

gh="gh --repo $GITHUB_REPO"
if ! $gh secret list > /dev/null
then
  echo "Cannot access repo $GITHUB_REPO"
  echo "$GITHUB_REPO must be a repository on which you have Contributor permissions."
  exit 1
fi

echo "== Creating resource group $ACR_RESOURCE_GROUP in location $ACR_RESOURCE_GROUP_LOCATION =="

az group create --name "$ACR_RESOURCE_GROUP" --location "$ACR_RESOURCE_GROUP_LOCATION" -o none

echo "== Creating Azure container registry $ACR_NAME in resource group $ACR_RESOURCE_GROUP =="

az acr create --resource-group "$ACR_RESOURCE_GROUP" --name "$ACR_NAME" --sku Basic -o none

$gh secret set ACR_NAME --body "$ACR_NAME"
$gh secret set AZURE_CLIENT_ID --body "$CD_CLIENT_ID"
$gh secret set AZURE_SUBSCRIPTION_ID --body "$subscription_id"
$gh secret set AZURE_TENANT_ID --body "$tenant_id"

$gh secret set EDC_CLIENT_ID --body "$EDC_CLIENT_ID"
$gh secret set EDC_CLIENT_SECRET --body "$EDC_CLIENT_SECRET"
