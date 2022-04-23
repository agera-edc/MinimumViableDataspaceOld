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

import io.restassured.specification.RequestSpecification;
import org.eclipse.dataspaceconnector.azure.blob.AzureBlobStoreSchema;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.policy.model.PolicyType;
import org.eclipse.dataspaceconnector.spi.asset.AssetSelectorExpression;
import org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static ch.qos.logback.core.util.OptionHelper.getEnv;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.system.tests.local.BlobTransferLocalSimulation.ACCOUNT_NAME_PROPERTY;
import static org.eclipse.dataspaceconnector.system.tests.local.TransferLocalSimulation.PROVIDER_MANAGEMENT_PATH;
import static org.eclipse.dataspaceconnector.system.tests.utils.GatlingUtils.runGatling;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.PROVIDER_ASSET_FILE;
import static org.eclipse.dataspaceconnector.system.tests.utils.TransferSimulationUtils.PROVIDER_ASSET_ID;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class BlobTransferIntegrationTest {
    private static final String ASSETS_PATH = "/assets";
    private static final String POLICIES_PATH = "/policies";
    private static final String CONTRACT_DEFINITIONS_PATH = "/contractdefinitions";
    private static final String PROVIDER_CONTAINER_NAME = UUID.randomUUID().toString();

    public static final String PROVIDER_CONNECTOR_MANAGEMENT_URL = getEnv("PROVIDER_DM_URL");
    public static final String SRC_ACCOUNT_NAME = "257company2assets"; //FIXME
    public static final String DST_ACCOUNT_NAME = "257company1assets"; //FIXME
    String account1Name = SRC_ACCOUNT_NAME;

    @Test
    public void transferBlob_success() {
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
        /*
        var container = getProvisionedContainerName();
        var destinationBlob = blobServiceClient2.getBlobContainerClient(container)
                .getBlobClient(PROVIDER_ASSET_NAME);
        assertThat(destinationBlob.exists())
                .withFailMessage("Destination blob %s not created", destinationBlob)
                .isTrue();
        var actualBlobContent = destinationBlob.downloadContent().toString();
        assertThat(actualBlobContent)
                .withFailMessage("Transferred file contents are not same as the source file")
                .isEqualTo(blobContent);
         */
    }

    /*
    private String getProvisionedContainerName() {
        JsonPath jsonPath = given()
                .baseUri(CONSUMER_CONNECTOR_MANAGEMENT_URL + CONSUMER_MANAGEMENT_PATH)
                .log().all()
                .when()
                .get(TRANSFER_PROCESSES_PATH)
                .then()
                .statusCode(200)
                .extract().body().jsonPath();
        return jsonPath.getString("[0].provisionedResources[0].dataAddress.properties.container");
    }
     */

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
                                AzureBlobStoreSchema.ACCOUNT_NAME, account1Name,
                                AzureBlobStoreSchema.CONTAINER_NAME, "src-container",
                                AzureBlobStoreSchema.BLOB_NAME, PROVIDER_ASSET_FILE,
                                "keyName", format("%s-key1", account1Name)
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
