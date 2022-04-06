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
- The following utilities installed locally:
  - [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli)
  - [GitHub CLI](https://cli.github.com)

### Log in to Azure & GitHub

- You must be [signed in to the target Azure subscription with the Azure CLI](https://docs.microsoft.com/cli/azure/authenticate-azure-cli) and [have the target Azure subscription selected](https://docs.microsoft.com/cli/azure/manage-azure-subscriptions-azure-cli).

- You must be [signed in to GitHub with the GitHub CLI](https://cli.github.com/manual/gh_auth_login) and must have Contributor permissions on the repository.

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

### Create a service identity for Applications

[Create and configure an Azure AD application for the application runtimes](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID.

Create a client secret by following the section "Create a new application secret" in the page on [Creating a an Azure AD application to access resources](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret). Take note of the client secret and keep it safe.

### Configure CD settings

The shell scripts that deploy resources take their configuration from a file named `.env` that should not be committed into the repository (though the file should be shared across developers in your fork). Copy and adapt the example settings file to your environment, following the comments in the file:

```bash
cp cd/.env.example cd/.env
```

### Deploying CD resources

The first time only, set up the container registry by running this script:

```bash
cd/initialize.sh
```

The script also configures your repository's GitHub secrets so that workflows can consume the resources. The following secrets are provisioned:

- `AZURE_CLIENT_ID` ,  `AZURE_SUBSCRIPTION_ID` and `AZURE_TENANT_ID`, required to log in with the Federated Credential scenario.
- `APP_CLIENT_ID`  and `APP_CLIENT_SECRET`, which together with  `AZURE_TENANT_ID` allows application runtimes to connect to Azure resources.
- `ACR_NAME` containing the container registry name.

Note that these values do not actually contain any sensitive information.

~                               
