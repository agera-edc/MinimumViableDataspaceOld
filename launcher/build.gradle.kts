/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
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

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val edcVersion: String by project
val edcGroup: String by project

dependencies {
    implementation(project(":extensions:refresh-catalog"))
    implementation("${edcGroup}:core:${edcVersion}")
    implementation("${edcGroup}:ids:${edcVersion}")
    implementation("${edcGroup}:control-api:${edcVersion}")
    implementation("${edcGroup}:observability-api:${edcVersion}")
    implementation("${edcGroup}:data-management-api:${edcVersion}")
    implementation("${edcGroup}:assetindex-memory:${edcVersion}")
    implementation("${edcGroup}:transfer-process-store-memory:${edcVersion}")
    implementation("${edcGroup}:contractnegotiation-store-memory:${edcVersion}")
    implementation("${edcGroup}:contractdefinition-store-memory:${edcVersion}")
    implementation("${edcGroup}:iam-mock:${edcVersion}")
    implementation("${edcGroup}:filesystem-configuration:${edcVersion}")
    implementation("${edcGroup}:http:${edcVersion}")
    implementation("${edcGroup}:policy-store-memory:${edcVersion}")

    // API key authentication (also used for CORS support)
    implementation("${edcGroup}:auth-tokenbased:${edcVersion}")

    // Blob storage container provisioning
    implementation("${edcGroup}:blobstorage:${edcVersion}")
    implementation("${edcGroup}:azure-vault:${edcVersion}")

    // Embedded DPF
    implementation("${edcGroup}:data-plane-transfer-client:${edcVersion}")
    implementation("${edcGroup}:data-plane-selector-client:${edcVersion}")
    implementation("${edcGroup}:data-plane-selector-store:${edcVersion}")
    implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")
    implementation("${edcGroup}:data-plane-framework:${edcVersion}")
    implementation("${edcGroup}:data-plane-azure-storage:${edcVersion}")

    // Federated catalog
    implementation("${edcGroup}:catalog-cache:${edcVersion}")
    implementation("${edcGroup}:catalog-node-directory-memory:${edcVersion}")
    implementation("${edcGroup}:catalog-cache-store-memory:${edcVersion}")

    implementation("${edcGroup}:ids-policy:${edcVersion}")
}

application {
    mainClass.set("org.eclipse.dataspaceconnector.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("dataspaceconnector-basic.jar")
}
