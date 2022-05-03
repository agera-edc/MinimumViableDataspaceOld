# Data Transfer

### Overview

Data Transfer in EDC is an operation by which a Consumer and a Provider connectors, having previously negotiated a Contract Agreement, execute the Contract Agreement to have the Provider transfer data to the Consumer.

The operation is initiated on the Consumer side. A transfer request is initiated on the Consumer side, sent over to the Provider, and executed in DPF. The Consumer provides the Provider with a data destination, such as a cloud object storage with temporary credentials.

The Provider does not notify the Consumer when a transfer has completed (succeeded or failed). Depending on the type of destination, the Consumer polls the output (e.g. cloud object storage) to wait for data to be produced.

