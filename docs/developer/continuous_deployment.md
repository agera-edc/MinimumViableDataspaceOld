## Continuous Deployment

### Overview

A cloud deployment.

- An **application** is created to represent the action runner that provisions cloud resources. In Azure Active Directory, a service principal for the application is configured in the cloud tenant, and configured to trust the GitHub repository using Federated Identity Credentials.

## Deploying an Azure environment

### Planning your deployment

You will need:

- An Azure subscription
- At least one developer with the `Owner` role on the Azure subscription in order to deploy resources and assign roles
- A service principal (instructions below)
- The following utilities installed locally:
  - [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli)
  - [GitHub CLI](https://cli.github.com)

### Log in to Azure & GitHub

- You must be [signed in to the target Azure subscription with the Azure CLI](https://docs.microsoft.com/cli/azure/authenticate-azure-cli) and [have the target Azure subscription selected](https://docs.microsoft.com/cli/azure/manage-azure-subscriptions-azure-cli).

- You must be [signed in to GitHub with the GitHub CLI](https://cli.github.com/manual/gh_auth_login) and must have Contributor permissions on the repository.

### Create a service identity for GitHub Actions

[Create and configure an application for GitHub Actions](https://docs.microsoft.com/azure/active-directory/develop/workload-identity-federation-create-trust-github).

Follow the instructions to *Create an app registration*.

- In **Supported Account Types**, select **Accounts in this organizational directory only**.
- Don't enter anything for **Redirect URI (optional)**.

Take note of the Application (client) ID.

Follow the instructions to *Configure a federated identity credential*.

- For **Entity Type**, select **Pull Request**.
- For **Name**, type any name.

### Configure CD settings

The shell scripts that deploy resources take their configuration from a file named `.env` that should not be committed into the repository (though the file should be shared across developers in your fork). Copy and adapt the example settings file to your environment, following the comments in the file:

```bash
cp cd/.env.example cd/.env
```

At a minimum, you must modify the following values:

- `GITHUB_REPO` (to reflect your fork)

### Deploying CD resources

The first time only, set up the container registry by running this script:

```bash
cd/initialize.sh
```

The script also configures your repository's GitHub secrets so that workflows can consume the resources. The following secrets are provisioned:

- `AZURE_CLIENT_ID` ,  `AZURE_SUBSCRIPTION_ID` and `AZURE_TENANT_ID`, required to log in with the Federated Credential scenario.
- `ACR_NAME` containing the container registry name.

Note that these values do not actually contain any sensitive information.

