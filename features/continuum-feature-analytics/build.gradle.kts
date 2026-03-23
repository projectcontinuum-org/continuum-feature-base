plugins {
    id("org.projectcontinuum.feature") version "0.0.9"
}

group = "org.projectcontinuum.feature.analytics"
description = "Continuum Feature Analytics - A feature for collecting and analyzing feature usage data in the Continuum platform."
version = property("featureVersion").toString()

// get continuum platform version from root project properties
val continuumPlatformVersion = property("continuumPlatformVersion").toString()
val kotlinScriptingVersion = "2.1.0"

continuum {
    continuumVersion.set(continuumPlatformVersion)
}

dependencies {
    // Kotlin scripting dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinScriptingVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinScriptingVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinScriptingVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinScriptingVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:$kotlinScriptingVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinScriptingVersion")

    // FreeMarker template engine
    implementation("org.freemarker:freemarker:2.3.32")
}