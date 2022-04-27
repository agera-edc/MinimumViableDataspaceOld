plugins {
    `java-library`
}

val edcVersion: String by project
val edcGroup: String by project

dependencies {
    api("${edcGroup}:catalog-cache:${edcVersion}")
}
