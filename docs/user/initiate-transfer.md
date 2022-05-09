# Initiating a Data Transfer

### Overview

Data Transfer in EDC is an operation by which a Consumer and a Provider connectors, having previously negotiated a Contract Agreement, execute the Contract Agreement to have the Provider transfer data to the Consumer.

The operation is initiated on the Consumer side. A transfer request is initiated on the Consumer side, sent over to the Provider, and executed in DPF. The Consumer provides the Provider with a data destination, such as a cloud object storage with temporary credentials.

The Provider does not notify the Consumer when a transfer has completed (succeeded or failed). Depending on the type of destination, the Consumer polls the output (e.g. cloud object storage) to wait for data to be produced.

## Prerequisites

To perform a data transfer, the Consumer must first have successfully executed a Contract Negotiation with the Provider.

## Initiating a transfer

In the *Contract Viewer* pane, click the *Transfer* button under an asset that was successfully negotiated. Select a destination (only *Azure Storage* is currently implemented). Click Start transfer to begin the process.

The UI displays the transfer state as it progresses. When the transfer reaches the *COMPLETED* state, a pop-up appears to indicate completion.

In the *Transfer History* pane you should see the transfer that was just performed. The "Open Storage Explorer" button opens the destination storage container in [Azure Storage Explorer](https://azure.microsoft.com/features/storage-explorer) (you will need to install the software beforehand if not available on your machine). You can see the created assets, as well as a `.complete` marker blob used for the Consumer to detect transfer completion.
