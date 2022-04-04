# User stories for MVD

This document details a couple of interaction avenues with the MVD, basically what "users can do with the MVD"

## [optional] Obtain signed claims from trust anchor
_As a dataspace participant I want verified (signed) claims from the dataspace's anchor of trust._


## 1. Create necessary identity documents

_As a dataspace participant I want to create my Web DID JSON file._

Every dataspace participant needs to create a JSON document containing the following items:
- a URL pointing to the participant's identity hub
- the participant's public key

That means, that a public/private key pair must be generated beforehand and offline. 
Furthermore, the URL of the identity hub must be known before. 

In the dataspace it is assumed that the `did:web` is publicly available for everyone as it does not contain private information. How that is achieved is out-of-scope of this document.

#### Preconditions
- Public/Private key pair is available
- a publicly accessible URL to the `did:web` is available 
- the URL to the identity hub is known

## 2. Register with dataspace ("onboarding")

_As a dataspace participant I want to register my connector with the dataspace._

The process of onboarding refers to the action of registering a connector with the registration service. In order to do that a REST request against the registration service has to be made containing the following information:
- the `did:web` ID (constructed using the URL point to the `web:did`)
- the name of the connector
- the IP-address/hostname of the connector
- the connector's self-description (todo: add details)

Upon receiving a successful response (HTTP 200) the connector is ready to participate in the dataspace.

#### Preconditions
- `web:did` is created and published
- connector's self-description is created
- connector runtime is deployed and publicly accessible


## 3. Upload a new asset

_As a dataspace participant I want to upload a new asset_

This action refers to creating an `Asset` and a `DataAddress` (pointing to the physical location of the data item) in the participant's connector through a REST API. At this time, other dataspace participants do _not yet_ have access to it!

The following steps are necessary:
2. generate a `DataAddress` containing relevant information (bucket name, accout name, etc.)
3. generate an `Asset` object describing the data item
4. call REST endpoint to insert the `Asset`/`DataAddress` tuple into the connector

In the first iteration of the MVD, composing the `Asset`/`DataAddress` is done through and aided by the connector's web UI using a simple text entry widget. 

Initially, we'll support `AzureStorage`, `AmazonS3` and `api`.

#### Preconditions
- data item (file) must already be physically available, e.g. a file on a data share, a private REST api, etc.


## 4. Upload a new Policy
_as a dataspace participant I want to formulate and upload a new policy to my connector_

A policy is essentially a container for a set of rules. The connector's web UI offers a widget to insert a policy and upload it to the connector's REST api.

As a simplification, there can be a pre-configured set of rules that the user can pick from, such as geo-restrictions, etc.

#### Preconditions:
- connector and web UI is deployed

## 5. Publish a new asset

_as a dataspace participant I want to make my asset available to all participants_

The act of publishing an asset refers to the creation of a `ContractDefintion`, or to the addition of the asset to an existing `ContractDefinition`.

Consequently, the connector's web UI should offer a way to list all existing `ContractDefinition`s and to either pick one, or to create a new one.

#### Preconditions
- asset is already uploaded
- the policy is already uploaded

## 6. View the dataspace catalog
_as a dataspace participant I want to look at all the data assets that all the other connectors offer me_

The connector's web UI offers a page to display all data offerings, which can be obtained through the connector's REST API.


## 7. Negotiate access to a particular asset
_as a dataspace participant I want to initiate a contract negotiation with another connector to gain access to a particular asset_

The web UI offers a button to trigger the negotiation for a particular asset. The connector should then store the resulting `ContractAgreement` for subsequent use.

## 8. Transfer an asset
_as a dataspace participant I want to select an asset, for which there is a contract, and initiate a data transfer_

The web UI displays a list of `ContractAgreement`s and displays a button to initiate a transfer. A dropdown is shown to select the data destination, which can be a fixed list for now (e.g. AzureStorage and AmazonS3).

Note: displaying the transferred asset by opening e.g. the S3 portal is out-of-scope of this document.

## 9. Look at existing contracts
_as a dataspace participant I want to be able to see all `ContractAgreement`s that I have already negotiated_

## 10. Look at all past transfers
_as a dataspace participant I want to be able to see all `TransferProcess`es that I have already transferred including ongoing ones_

## Delete/Revoke a contract (?)

## Unregister from dataspace ("offboarding")