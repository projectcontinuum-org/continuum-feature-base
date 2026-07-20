pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    val continuumPlatformVersion = providers.gradleProperty("continuumPlatformVersion").get()
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.projectcontinuum.worker" || requested.id.id == "org.projectcontinuum.feature") {
                useVersion(continuumPlatformVersion)
            }
        }
    }
}

rootProject.name = "continuum-feature-base"

include(":features:continuum-feature-analytics")
include(":worker")
