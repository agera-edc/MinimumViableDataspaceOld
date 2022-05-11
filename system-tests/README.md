## System tests

The test use the key vault secret to connect to the storage accounts and copy a file
from provider to consumer storage account.

### Running test locally

Deploy MVD using the GitHub `Deploy` pipeline. We will run EDC instances locally, connected to the storage accounts and key vaults deployed on Azure.

From the build result, download the artifact named `testing-configuration` and extract the file `.env` into
the `system-tests` directory (note that the file could be hidden in your file explorer due to its prefix).

In the file, add the application client secret value under the `EDC_VAULT_CLIENTSECRET` key.

Build the EDC launcher:

```
./gradlew :launcher:shadowJar
```

Run EDC consumer, provider and data seeding:

```
docker-compose -f system-tests/docker-compose.yml up --build
```

In the command below, adapt the variable value to use the value from the `.env` file.

Run tests:
```
CONSUMER_KEY_VAULT={key_vault_name} ./gradlew :system-tests:test
```
