## System tests

The test use the key vault secret to connect to the storage accounts and copy a file
from provider to consumer storage account.

### Running test locally

Deploy MVD using the GitHub pipeline, adapting it in a branch to skip the destroy step.
This leaves a storage account and a key vault for each of the consumer and the provider.

From the build result, download the artifact named `testing-configuration` and extract the file `.env` into
the `system-tests` directory (note that the file could be hidden in your file explorer due to its prefix).

In the file, add the application client secret value.

Build and run EDC consumer and provider:

```
./gradlew :launcher:shadowJar

docker-compose -f system-tests/docker-compose.yml up --build
```

In the command below, adapt the variable value to use the value from the `.env` file.

Run test:
```
CONSUMER_KEY_VAULT={key_vault_name} ./gradlew :system-tests:test
```
