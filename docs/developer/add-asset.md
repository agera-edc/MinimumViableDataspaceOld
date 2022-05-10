# Add Asset

## Overview

A new asset can be added through MVD UI, in this process we actually point to an pre-existing asset in participant's respective object storage account. In this version of MVD, only Azure Object Storage is supported.

So for instance if we want to add and asset named as `test-document` then this asset must be already available in the participant's object storage account under container named as `src-container` before we can add it via MVD UI. Refer to the following link for more details about how to upload an asset in Azure Object Storage:

* [Create a container](https://docs.microsoft.com/azure/storage/blobs/storage-quickstart-blobs-portal#create-a-container)
* [Upload a blob](https://docs.microsoft.com/azure/storage/blobs/storage-quickstart-blobs-portal#upload-a-block-blob)

## Prerequisites

Actual asset must be already uploaded in object storage account under container named as `src-container`.

## Adding an asset

* Go to`Assets` pane and here you will see all existing assets if any.
* Click on `Create asset` button and this will open an UI popup where you can add further details.
* Add a unique ID e.g. UUID for the asset in text box named `id`.
* Add name for the asset in text box named `name`.
* Add content type for the asset in text box named `Content Type`. e.g. `text/plain`
* `Azure Storage` must be pre-selected in the dropdown named as `Destination`.
* Add object storage account name in text box named as `Account`.
* `src-container` must pre-filled in the text box named as `Container`.
* Add blob name in text box named as `Blob Name`.
* Click on `Create` button to add the asset.
* `Assets` pane will be refreshed and you will see the newly created asset.
