## Continuous Deployment

### Overview

A GitHub Actions workflow performs continuous integration and continuous deployment of the MVD to an Azure subscription. The workflow needs the following infrastructure to be deployed:

- An **application** is created to represent the action runner that provisions cloud resources. In Azure Active Directory, a service principal for the application is configured in the cloud tenant, and configured to trust the GitHub repository using Federated Identity Credentials.
- Another **application** is created to represent the deployed runtimes for accessing Azure resources (such as Key Vault). For simplicity, all runtimes share a single application identity. In Azure Active Directory, a service principal for the application is configured in the cloud tenant. A client secret is configured to allow the runtime to authenticate.
- An **Azure Container Registry** instance is deployed to contain docker images built in the CI job. These images are deployed to runtime environments in the CD process.
- An **Azure Storage Account** and a storage container to store **Terraform state** between the deployment and destroy jobs.
- An **Azure Storage Account** and a file share to store JSON files representing the **Dataspace Registry** across multiple participants.

## Initializing an Azure environment for CD

### Planning your deployment

You will need to provide the following:

- An Azure subscription
- Two service principals (instructions below)

A GitHub workflow then needs to be run to provision the Azure resources used for CD.

### Create a service identity for GitHub Actions

[Create and configure an Azure AD application for GitHub Actions](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID.

Below, we create two credentials: one for federated authentication for GitHub Actions, and one with client secret for Terraform (required as Terraform does not yet support Azure CLI login with a service principal).

Follow the instructions to [Configure a federated identity credential]([Configure a federated identity credential](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github?tabs=azure-portal#configure-a-federated-identity-credential)) for the `main` branch.

- For **Entity Type**, select **Branch**.
- For **GitHub branch name**, enter `main`.
- For **Name**, type any name.

Follow the instructions to [Configure a federated identity credential](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github?tabs=azure-portal#configure-a-federated-identity-credential) for Pull requests.

Configure the following GitHub secrets:
- For **Entity Type**, select **Pull Request**.
- For **Name**, type any name.

Create a client secret by following the section "Create a new application secret" in the page on [Creating a an Azure AD application to access resources](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret). Take note of the client secret and keep it safe.

[Grant the application Owner permissions](https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal) on your Azure subscription.

Configure the following GitHub secrets:

| Secret name         | Value                          |
| ------------------- | ------------------------------ |
| `ARM_CLIENT_ID`     | The application (client) ID.   |
| `ARM_CLIENT_SECRET` | The application client secret. |

### Create a service identity for Applications

[Create and configure an Azure AD application for the application runtimes](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID. Navigate to the corresponding *Managed application in local directory* (*Enterprise Application*) and take note of the enterprise application Object ID. ⚠️ Make sure you do not use the Object ID of the *App Registration*!

In the *App Registration*, create a client secret by following the section "Create a new application secret" in the page on [Creating a an Azure AD application to access resources](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret). Take note of the client secret and keep it safe.

Configure the following GitHub secrets:

| Secret name         | Value                          |
| ------------------- | ------------------------------ |
| `APP_CLIENT_ID`     | The application (client) ID.   |
| `APP_CLIENT_SECRET` | The application client secret. |
| `APP_OBJECT_ID`     | The ID of the service principal object associated with this application.     |

### Configure CD settings

Configure the following GitHub secrets:

| Secret name                   | Value                                                        |
| ----------------------------- | ------------------------------------------------------------ |
| `ARM_TENANT_ID`               | The Azure AD tenant ID.                                      |
| `ARM_SUBSCRIPTION_ID`         | The Azure subscription ID to deploy resources in.            |
| `COMMON_RESOURCE_GROUP`          | The Azure resource group name to deploy common resources in, such as Azure Container Registry and Dataspace Registry storage account. |
| `COMMON_RESOURCE_GROUP_LOCATION` | The location of the Azure resource group name to deploy common resources in. Example: `northeurope`. |
| `ACR_NAME`                    | The name of the Azure Container Registry to deploy. Use only lowercase letters and numbers. |
| `TERRAFORM_STATE_STORAGE_ACCOUNT` | The name of the storage account used to store the Terraform state container. |
| `TERRAFORM_STATE_CONTAINER` | The name of the container used to store the Terraform state blob. |
| `REGISTRY_STORAGE_ACCOUNT` | The name of the storage account used to store the Dataspace Registry file share. |
| `REGISTRY_SHARE` | The name of the file share used to store the Dataspace Registry files. |

Update the value of the `CD_RESOURCES_PREFIX` env in the [cd.yaml](./.github/workflows/cd.yaml) file.
This prefix should help have unique resource names across fork repositories when running CD workflow.

### Deploying CD resources

Manually run the `Initialize CD` GitHub Actions workflow.

### Deploying Data Dashboard

Fork the **EDC Data Dashboard** web app and run its deploy action.

Adapt the default value of the Data Dashboard image tag (`data_dashboard_image_tag` variable) in the [Terraform variables](deployment/terraform/variables.tf) to reflect the tag of the **EDC Data Dashboard** web app deployment.
