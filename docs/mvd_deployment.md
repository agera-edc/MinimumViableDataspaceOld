# Deployment scenario for MVD

This describes the **initial** deployment diagram of the minimal viable dataspace, i.e. the initial setup when the dataspace gets created. Being able to deploy a dataspace "on button click" will come in handy when people want to setup their own dataspace, when sales people need to show something on their laptops
or when we want to reset our "public" dataspace every day at 00:00.

We envision 3 participants initially, with different configurations each. Each participant will represent a ficticious company, called "Galactic Turbine Industries", "Space Enforcer Laser Tec" and "Interstellar Assembly Services" 
(names are open for discussion of course).

Whenever we do a reset of the entire dataspace, this setup it what it _resets to_.

## Deployment platforms

In the very beginning, we propose to host all runtimes everying in Azure using Terraform, but gradually we can move certain parts to other platforms such as Amazon, or even custom 
deployments, for instance on a Linode Linux server.

People can then onboard into the dataspace using their own runtimes.

## General Assumptions

For the sake of simplicity we'll assume that every connector runtime will:
- use only in-memory storage backends (as opposed to: CosmosDB, SQL, etc.)
- use Azure Vaults as secrets storage
- DPF Selectors run embedded and point to a configurable DFP instance
- run as single-instance in a K8S cluster (to enable scale-out later)
- have a did:web file readily available for deployment
- use an embedded identity hub
- have its own catalog (no hybrid deployments), regardless whether embedded or stand-alone

## "Galactic Turbine Industries"

K8S Cluster consisting of:
- Connector Runtime
- DPF Runtime (3 replica)
- Catalog Runtime
- Connector UI
- Cloudflare CDN containing web:did

Catalog runtime is deployed as standalone container.

## "Space Enforcer Laser Tec"
K8S Cluster consisting of:
- Connector + embedded catalog runtime
- DPF Runtime (1 replica)
- Connector UI
- Azure Storage Blob containing web:did

## "Interstellar Assembly Services"
K8S Cluster consisting of:
- Connector + embedded catalog runtime
- DPF Runtime (3 replica)
- Connector UI
- S3 Bucket containing web:did