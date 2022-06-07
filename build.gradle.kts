plugins {
    java
    `java-library`
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        // prepare for switch to using pinned snapshot builds
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
         }
        maven {
            url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}
