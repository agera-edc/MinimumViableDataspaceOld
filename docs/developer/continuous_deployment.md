## Continuous Deployment

### Overview

A GitHub Actions workflow performs continuous integration and continuous deployment of the MVD to an Azure subscription. The workflow needs the following infrastructure to be deployed:

- An **application** is created to represent the action runner that provisions cloud resources. In Azure Active Directory, a service principal for the application is configured in the cloud tenant, and configured to trust the GitHub repository using Federated Identity Credentials.
- Another **application** is created to represent the deployed runtimes for accessing Azure resources (such as Key Vault). For simplicity, all runtimes share a single application identity. In Azure Active Directory, a service principal for the application is configured in the cloud tenant. A client secret is configured to allow the runtime to authenticate.
- An **Azure Container Registry** instance is deployed to contain docker images built in the CI job. These images are deployed to runtime environments in the CD process.

## Initializing an Azure environment for CD

### Planning your deployment

You will need:

- An Azure subscription
- Two service principals (instructions below)

### Create a service identity for GitHub Actions

[Create and configure an Azure AD application for GitHub Actions](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID.

Follow the instructions to *Configure a federated identity credential*.

- For **Entity Type**, select **Pull Request**.
- For **Name**, type any name.

[Grant the application Owner permissions](https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal) on your Azure subscription.

Configure the following GitHub secrets:

| Secret name             | Value |
| ----------------------- | ----- |
| `AZURE_CLIENT_ID`       |       |
| `AZURE_SUBSCRIPTION_ID` |       |
| `AZURE_TENANT_ID`       |       |

### Create a service identity for Applications

[Create and configure an Azure AD application for the application runtimes](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID.

Create a client secret by following the section "Create a new application secret" in the page on [Creating a an Azure AD application to access resources](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret). Take note of the client secret and keep it safe.

Configure the following GitHub secrets:

| Secret name         | Value |
| ------------------- | ----- |
| `APP_CLIENT_ID`     |       |
| `APP_CLIENT_SECRET` |       |

### Configure CD settings

Configure the following GitHub secrets:

| Secret name                   | Value |
| ----------------------------- | ----- |
| `ACR_RESOURCE_GROUP`          |       |
| `ACR_RESOURCE_GROUP_LOCATION` |       |
| `ACR_NAME`                    |       |

### Deploying CD resources

Manually run the CD pipeline.
