/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.system.tests.utils;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.policy.model.PolicyType;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataFlowRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.doWhileDuring;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.group;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static java.lang.String.format;

/**
 * Utility methods for building a Gatling simulation for performing contract negotiation and file transfer.
 */
public abstract class FileTransferSimulationUtils {

    public static final String CONTRACT_AGREEMENT_ID = "contractAgreementId";
    public static final String CONTRACT_NEGOTIATION_REQUEST_ID = "contractNegotiationRequestId";
    public static final String TRANSFER_PROCESS_ID = "transferProcessId";

    public static final String DESCRIPTION = "[Contract negotiation and file transfer]";

    public static final String PROVIDER_ASSET_NAME = "text-document";
    private static final String CONTRACT_DEFINITION_ID = "4a75736e-001d-4364-8bd4-9888490edb56";

    public static final String THE_ACCOUNT_NAME = "testdocument";
    public static final String THE_CONTAINER_NAME = "testdocument";

    private FileTransferSimulationUtils() {
    }

    /**
     * Gatling chain for performing contract negotiation and file transfer.
     *
     * @param providerUrl     URL for the Provider API, as accessed from the Consumer runtime.
     */
    public static ChainBuilder contractNegotiationAndFileTransfer(String providerUrl) {
        return startContractAgreement(providerUrl)
                .exec(waitForContractAgreement())
                .exec(startFileTransfer(providerUrl))
                .exec(waitForTransferCompletion());
    }

    /**
     * Gatling chain for initiating a contract negotiation.
     * <p>
     * Saves the Contract Negotiation Request ID into the {@see CONTRACT_NEGOTIATION_REQUEST_ID} session key.
     *
     * @param providerUrl URL for the Provider API, as accessed from the Consumer runtime.
     */
    private static ChainBuilder startContractAgreement(String providerUrl) {
        var connectorAddress = format("%s/api/v1/ids/data", providerUrl);
        return group("Contract negotiation")
                .on(exec(initiateContractNegotiation(connectorAddress)));
    }

    private static HttpRequestActionBuilder initiateContractNegotiation(String connectorAddress) {
        return http("Initiate contract negotiation")
                .post("/contractnegotiations")
                .body(StringBody(loadContractAgreement(connectorAddress)))
                .header(CONTENT_TYPE, "application/json")
                .check(status().is(200))
                .check(bodyString()
                        .notNull()
                        .saveAs(CONTRACT_NEGOTIATION_REQUEST_ID));
    }

    /**
     * Gatling chain for calling ContractNegotiation status endpoint repeatedly until a CONFIRMED state is
     * attained, or a timeout is reached.
     * <p>
     * Expects the Contract Negotiation Request ID to be provided in the {@see CONTRACT_NEGOTIATION_REQUEST_ID} session key.
     * <p>
     * Saves the Contract Agreement ID into the {@see CONTRACT_AGREEMENT_ID} session key.
     */
    private static ChainBuilder waitForContractAgreement() {
        return exec(session -> session.set("status", -1))
                .group("Wait for agreement")
                .on(doWhileDuring(session -> session.getString(CONTRACT_AGREEMENT_ID) == null, Duration.ofSeconds(30))
                        .on(exec(getContractStatus()).pace(Duration.ofSeconds(1)))
                );
    }

    private static HttpRequestActionBuilder getContractStatus() {
        return http("Get status")
                .get(session -> format("/contractnegotiations/%s", session.getString(CONTRACT_NEGOTIATION_REQUEST_ID)))
                .check(status().is(200))
                .check(
                        jmesPath("id").is(session -> session.getString(CONTRACT_NEGOTIATION_REQUEST_ID)),
                        jmesPath("state").saveAs("status")
                )
                .checkIf(
                        session -> ContractNegotiationStates.CONFIRMED.name().equals(session.getString("status"))
                ).then(
                        jmesPath("contractAgreementId").notNull().saveAs(CONTRACT_AGREEMENT_ID)
                );
    }

    /**
     * Gatling chain for initiating a file transfer request.
     * <p>
     * Expects the Contract Agreement ID to be provided in the {@see CONTRACT_AGREEMENT_ID} session key.
     * <p>
     * Saves the Transfer Process ID into the {@see TRANSFER_PROCESS_ID} session key.
     *
     * @param providerUrl     URL for the Provider API, as accessed from the Consumer runtime.
     */
    private static ChainBuilder startFileTransfer(String providerUrl) {
        String connectorAddress = format("%s/api/v1/ids/data", providerUrl);
        return group("Initiate transfer")
                .on(exec(initiateFileTransfer(connectorAddress)));
    }

    private static HttpRequestActionBuilder initiateFileTransfer(String connectorAddress) {

        return http("Initiate file transfer")
                .post("/transferprocess")
                .body(StringBody(session -> transferRequest(session.getString(CONTRACT_AGREEMENT_ID), connectorAddress)))
                .header(CONTENT_TYPE, "application/json")
                .check(status().is(200))
                .check(bodyString()
                        .notNull()
                        .saveAs(TRANSFER_PROCESS_ID));
    }

    private static String transferRequest(String contractAgreementId, String connectorAddress) {
        /*
        var request = Map.of(
                "contractId", contractAgreementId,
                "assetId", PROVIDER_ASSET_NAME,
                "connectorId", "consumer",
                "connectorAddress", connectorAddress,
                "protocol", "ids-multipart",
                "dataDestination", DataAddress.Builder.newInstance()
                        .keyName("keyName")
                        .type("File")
                        .property("path", destinationPath)
                        .build(),
                "managedResources", false,
                "transferType", TransferType.Builder.transferType()
                        .contentType("application/octet-stream")
                        .isFinite(true)
                        .build()
        );
                */


        var blobName = UUID.randomUUID().toString();
                var destinationDataAddress = DataAddress.Builder.newInstance()
                        .type(TYPE)
                        .property(ACCOUNT_NAME, THE_ACCOUNT_NAME)
                        .property(CONTAINER_NAME, THE_CONTAINER_NAME)
                        .property(BLOB_NAME, blobName)
                        .build();

        var request = Map.of(
                "contractId", contractAgreementId,
                "assetId", PROVIDER_ASSET_NAME,
                "connectorId", "consumer",
                "connectorAddress", connectorAddress,
                "protocol", "ids-multipart",
                "dataDestination", destinationDataAddress,
                "managedResources", true, // FIXME ?
                "transferType", TransferType.Builder.transferType() // FIXME?
                        .contentType("application/octet-stream")
                        .isFinite(true)
                        .build()
        );

        return new TypeManager().writeValueAsString(request);

    }

    /**
     * Gatling chain for calling the transfer status endpoint repeatedly until a COMPLETED state is
     * attained, or a timeout is reached.
     * <p>
     * Expects the Transfer Process ID to be provided in the {@see TRANSFER_PROCESS_ID} session key.
     */
    private static ChainBuilder waitForTransferCompletion() {
        return group("Wait for transfer").on(
                exec(session -> session.set("status", "INITIAL"))
                        .doWhileDuring(session -> !session.getString("status").equals(TransferProcessStates.COMPLETED.name()),
                                Duration.ofSeconds(30))
                        .on(exec(getTransferStatus()).pace(Duration.ofSeconds(1)))
        );
    }

    private static HttpRequestActionBuilder getTransferStatus() {
        return http("Get transfer status")
                .get(session -> format("/transferprocess/%s", session.getString(TRANSFER_PROCESS_ID)))
                .check(status().is(200))
                .check(
                        jmesPath("id").is(session -> session.getString(TRANSFER_PROCESS_ID)),
                        jmesPath("state").saveAs("status")
                );
    }

    private static String loadContractAgreement(String providerUrl) {
        var policy = Policy.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .permission(Permission.Builder.newInstance()
                        .target("text-document")
                        .action(Action.Builder.newInstance().type("USE").build())
                        .build())
                .type(PolicyType.SET)
                .build();
        var request = Map.of(
                "connectorId", "provider",
                "connectorAddress", providerUrl,
                "protocol", "ids-multipart",
                "offer", Map.of(
                        "offerId", CONTRACT_DEFINITION_ID + ":1",
                        "assetId", PROVIDER_ASSET_NAME,
                        "policy", policy
                )
        );

        return new TypeManager().writeValueAsString(request);
    }


    public static final String TYPE = "AzureStorageBlobData";
    public static final String ACCOUNT_NAME = "account";
    public static final String CONTAINER_NAME = "container";
    public static final String BLOB_NAME = "blob";
    public static final String SHARED_KEY = "sharedKey";

    /*
    private void gen() {
        var tenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";
        var subscriptionId = "9d236f09-93d9-4f41-88a6-20201a6a1abc";
        var PROVIDER_STORAGE_RESOURCE_ID = "/subscriptions/9d236f09-93d9-4f41-88a6-20201a6a1abc/resourceGroups/rg-company1-194/providers/Microsoft.Storage/storageAccounts/194company1";
        var CONSUMER_STORAGE_RESOURCE_ID = "/subscriptions/9d236f09-93d9-4f41-88a6-20201a6a1abc/resourceGroups/rg-company1-194/providers/Microsoft.Storage/storageAccounts/194company1";

        // Detect credential source based on runtime environment, e.g. Azure CLI, environment variables
        var credential = new DefaultAzureCredentialBuilder().build();

        var azure = AzureEnvironment.AZURE;
        var profile = new AzureProfile(tenantId, subscriptionId, azure);

        var azureResourceManager = AzureResourceManager
                .authenticate(credential, profile)
                .withSubscription(subscriptionId);

            var providerStorage = new Account(azureResourceManager, PROVIDER_STORAGE_RESOURCE_ID);
            var consumerStorage = new Account(azureResourceManager, CONSUMER_STORAGE_RESOURCE_ID);
            var randomBytes = new byte[1024];
            var random = new Random();
            random.nextBytes(randomBytes);

            var source = DataAddress.Builder.newInstance()
                    .type(TYPE)
                    .property(ACCOUNT_NAME, providerStorage.name)
                    .property(CONTAINER_NAME, providerStorage.containerName)
                    .property(BLOB_NAME, blobName)
                    .property(SHARED_KEY, providerStorage.key)
                    .build();
            var destination = DataAddress.Builder.newInstance()
                    .type(TYPE)
                    .property(ACCOUNT_NAME, consumerStorage.name)
                    .property(CONTAINER_NAME, consumerStorage.containerName)
                    .property(SHARED_KEY, consumerStorage.key)
                    .build();
            var request = DataFlowRequest.Builder.newInstance()
                    .sourceDataAddress(source)
                    .destinationDataAddress(destination)
                    .id(UUID.randomUUID().toString())
                    .processId(UUID.randomUUID().toString())
                    .trackable(true)
                    .build();
        }

     */
            /*

        private String contractId;
        private String assetId;
        private String connectorId;
        private String connectorAddress;
        private String protocol = "ids-multipart";
        private DataAddress dataDestination;
        private boolean managedResources = true;
        private Map<String, String> properties = new HashMap<>();
        private TransferType transferType;
*/


            /*
            // Act
            dataPlaneManager.initiateTransfer(request);

            // Assert
            var destinationBlob = consumerStorage.client
                    .getBlobContainerClient(consumerStorage.containerName)
                    .getBlobClient(blobName);
            await()
                    .atMost(Duration.ofMinutes(5))
                    .untilAsserted(() -> assertThat(store.getState(request.getProcessId()))
                            .isEqualTo(DataPlaneStore.State.COMPLETED));
            assertThat(destinationBlob.exists())
                    .withFailMessage("should have copied blob between containers")
                    .isTrue();
            assertThat(destinationBlob.getProperties().getBlobSize())
                    .isEqualTo(randomBytes.length);

             */

        /*
        static class Account {

            static final Faker FAKER = new Faker();
            final String name;
            final String key;
            final BlobServiceClient client;
            final String containerName = FAKER.lorem().characters(35, 40, false, false);

            Account(AzureResourceManager azureResourceManager, String setting) {
                var account = azureResourceManager.storageAccounts().getById(accountId);
                name = account.name();
                key = account.getKeys().stream().findFirst().orElseThrow().value();
                client = new BlobServiceClientBuilder()
                        .credential(new StorageSharedKeyCredential(account.name(), key))
                        .endpoint(account.endPoints().primary().blob())
                        .buildClient();
                createContainer();
            }

            void createContainer() {
                assertFalse(client.getBlobContainerClient(containerName).exists());

                BlobContainerClient blobContainerClient = client.createBlobContainer(containerName);
                assertTrue(blobContainerClient.exists());
                containerCleanup.add(() -> client.deleteBlobContainer(containerName));
            }
        }

         */

}
