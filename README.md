# MinimumViableDataspace

Temporary repository to get started with the MVD

## Set up your own MVD fork repository.

Follow the instructions [to set up your own MVD fork repository](docs/developer/continuous_deployment.md).

## Deploy your own DataSpace.

To be able to deploy your own DataSpace, you need first to create your own MVD fork repository.

- Go to your github repository MVD fork.
- Select the tab called `Actions`.
- Select the workflow called `Deploy`.
- Click on `Run workflow`.
- Provide your own resources name prefix. This name prefix is used to guarantee uniquess of resources name and avoid 
resources name conflicts. Note down the used prefix.
- Click on `Run workflow` to trigger the deployment.

## Destroy your deployed DataSpace.

You might need to delete the DataSpace created previously.

Go to your github repository MVD fork,
- Select the tab called `Actions`
- Select the workflow called `Destroy`
- Click on `Run workflow`
- Provide the resources prefix that you used when you deployed your DataSpace.
- Click on `Run workflow` to trigger to destroy your MVD DataSpace.