plugins {
    id("org.projectcontinuum.worker") version "0.0.8"
}

group = "org.projectcontinuum.feature.base"
description = "Continuum Feature Base Worker — Spring Boot worker application for base feature nodes"
version = property("featureVersion").toString()

// get continuum platform version from root project properties
val continuumPlatformVersion = property("continuumPlatformVersion").toString()

continuum {
    continuumVersion.set(continuumPlatformVersion)
}

dependencies {
    // Feature node modules (local project)
    implementation(project(":features:continuum-feature-analytics"))
}
