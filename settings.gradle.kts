pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "continuum-feature-base"

include(":features:continuum-feature-analytics")
include(":worker")
