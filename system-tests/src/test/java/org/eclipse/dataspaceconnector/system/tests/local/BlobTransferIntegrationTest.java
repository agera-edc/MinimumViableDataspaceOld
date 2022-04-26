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
 *       ZF Friedrichshafen AG - add management api configurations
 *       Fraunhofer Institute for Software and Systems Engineering - added IDS API context
 *
 */

package org.eclipse.dataspaceconnector.system.tests.local;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.RequestSpecification;
import org.eclipse.dataspaceconnector.azure.blob.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.policy.model.PolicyType;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ch.qos.logback.core.util.OptionHelper.getEnv;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.system.tests.local.BlobTransferLocalSimulation.ACCOUNT_NAME_PROPERTY;
import static org.eclipse.dataspaceconnector.system.tests.local.TransferLocalSimulation.CONSUMER_MANAGEMENT_PATH;
import static org.eclipse.dataspaceconnector.system.tests.local.TransferLocalSimulation.PROVIDER_MANAGEMENT_PATH;
import static org.eclipse.dataspaceconnector.system.tests.utils.GatlingUtils.runGatling;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.PROVIDER_ASSET_FILE;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.PROVIDER_ASSET_ID;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.TRANSFER_PROCESSES_PATH;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class BlobTransferIntegrationTest {
    private static final String ASSETS_PATH = "/assets";
    private static final String POLICIES_PATH = "/policies";
    private static final String CONTRACT_DEFINITIONS_PATH = "/contractdefinitions";
    private static final String PROVIDER_CONTAINER_NAME = UUID.randomUUID().toString();

    public static final String PROVIDER_CONNECTOR_MANAGEMENT_URL = getEnv("PROVIDER_MANAGEMENT_URL");
    public static final String CONSUMER_CONNECTOR_MANAGEMENT_URL = getEnv("CONSUMER_MANAGEMENT_URL");
    public static final String SRC_ACCOUNT_NAME = "257company2assets"; //FIXME
    public static final String DST_ACCOUNT_NAME = "257company1assets"; //FIXME
    public static final String DST_KEY_VAULT_NAME = "kv257company1"; //FIXME
    String BLOB_STORE_ENDPOINT_TEMPLATE = "https://%s.blob.core.windows.net";
    String KEY_VAULT_ENDPOINT_TEMPLATE = "https://%s.vault.azure.net";


    @Test
    public void transferBlob_success() {
        BlobServiceClient blobServiceClient2 = getBlobServiceClient(DST_KEY_VAULT_NAME, DST_ACCOUNT_NAME);

        // Arrange
        // Seed data to provider
        createAsset();
        var policyId = createPolicy();
        createContractDefinition(policyId);

        // Write Key to vault

        // Act
        System.setProperty(ACCOUNT_NAME_PROPERTY, DST_ACCOUNT_NAME);
        runGatling(BlobTransferLocalSimulation.class, TransferSimulationUtils.DESCRIPTION);

        // Assert
        var container = getProvisionedContainerName();
        var destinationBlob = blobServiceClient2.getBlobContainerClient(container)
                .getBlobClient(PROVIDER_ASSET_FILE);
        assertThat(destinationBlob.exists())
                .withFailMessage("Destination blob %s not created", destinationBlob)
                .isTrue();
    }

    @NotNull
    private BlobServiceClient getBlobServiceClient(String keyVaultName, String accountName) {
        var credential = new DefaultAzureCredentialBuilder().build();
        var vault = new SecretClientBuilder()
                .vaultUrl(format(KEY_VAULT_ENDPOINT_TEMPLATE, keyVaultName))
                .credential(credential)
                .buildClient();
        var accountKey = vault.getSecret(accountName + "-key1");
        var blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(format(BLOB_STORE_ENDPOINT_TEMPLATE, accountName))
                .credential(new StorageSharedKeyCredential(accountName, accountKey.getValue()))
                .buildClient();
        return blobServiceClient;
    }

    private String getProvisionedContainerName() {
        ResponseBodyExtractionOptions body = given()
                .baseUri(CONSUMER_CONNECTOR_MANAGEMENT_URL + CONSUMER_MANAGEMENT_PATH)
                .log().all()
                .when()
                .get(TRANSFER_PROCESSES_PATH)
                .then()
                .statusCode(200)
                .extract().body();
        return body
                .jsonPath().getString("[0].dataDestination.container");
    }

    private void createAsset() {
        var asset = Map.of(
                "asset", Map.of(
                        "properties", Map.of(
                                "asset:prop:name", PROVIDER_ASSET_ID,
                                "asset:prop:contenttype", "text/plain",
                                "asset:prop:version", "1.0",
                                "asset:prop:id", PROVIDER_ASSET_ID,
                                "type", "AzureStorage"
                        )
                ),
                "dataAddress", Map.of(
                        "properties", Map.of(
                                "type", AzureBlobStoreSchema.TYPE,
                                AzureBlobStoreSchema.ACCOUNT_NAME, SRC_ACCOUNT_NAME,
                                AzureBlobStoreSchema.CONTAINER_NAME, "src-container",
                                AzureBlobStoreSchema.BLOB_NAME, PROVIDER_ASSET_FILE,
                                "keyName", format("%s-key1", SRC_ACCOUNT_NAME)
                        )
                )
        );

        seedProviderData(ASSETS_PATH, asset);
    }

    private String createPolicy() {
        var policy = Policy.Builder.newInstance()
                .permission(Permission.Builder.newInstance()
                        .target(PROVIDER_ASSET_ID)
                        .action(Action.Builder.newInstance().type("USE").build())
                        .build())
                .type(PolicyType.SET)
                .build();

        seedProviderData(POLICIES_PATH, policy);

        return policy.getUid();
    }

    private void createContractDefinition(String policyId) {

        var criteria = AssetSelectorExpression.Builder.newInstance()
                .constraint("asset:prop:id",
                        "=",
                        PROVIDER_ASSET_ID)
                .build();

        var contractDefinition = Map.of(
                "id", "1",
                "accessPolicyId", policyId,
                "contractPolicyId", policyId,
                "criteria", criteria.getCriteria()
        );

        seedProviderData(CONTRACT_DEFINITIONS_PATH, contractDefinition);
    }

    private void seedProviderData(String path, Object requestBody) {
        givenProviderBaseRequest()
                .log().all()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post(path)
                .then()
                .statusCode(anyOf(is(204), is(409)));
    }

    private RequestSpecification givenProviderBaseRequest() {
        return given()
                .baseUri(PROVIDER_CONNECTOR_MANAGEMENT_URL + PROVIDER_MANAGEMENT_PATH);
    }
}
