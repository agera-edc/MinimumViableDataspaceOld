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
import org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static ch.qos.logback.core.util.OptionHelper.getEnv;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.system.tests.local.BlobTransferLocalSimulation.ACCOUNT_NAME_PROPERTY;
import static org.eclipse.dataspaceconnector.system.tests.local.TransferLocalSimulation.CONSUMER_MANAGEMENT_PATH;
import static org.eclipse.dataspaceconnector.system.tests.utils.GatlingUtils.runGatling;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.PROVIDER_ASSET_FILE;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.TRANSFER_PROCESSES_PATH;

public class BlobTransferIntegrationTest {
    public static final String CONSUMER_CONNECTOR_MANAGEMENT_URL = getEnv("CONSUMER_MANAGEMENT_URL");
    public static final String DST_ACCOUNT_NAME = getEnv("CONSUMER_STORAGE_ACCOUNT");
    public static final String DST_KEY_VAULT_NAME = getEnv("CONSUMER_KEY_VAULT");
    String BLOB_STORE_ENDPOINT_TEMPLATE = "https://%s.blob.core.windows.net";
    String KEY_VAULT_ENDPOINT_TEMPLATE = "https://%s.vault.azure.net";


    @Test
    public void transferBlob_success() {
        BlobServiceClient blobServiceClient2 = getBlobServiceClient(DST_KEY_VAULT_NAME, DST_ACCOUNT_NAME);

        // Arrange
        // Seed data to provider

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
}
