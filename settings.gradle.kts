pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
    id("dev.kikugie.loom-back-compat") version "0.3"
}

stonecutter {
    create(rootProject) {
        version("26.1", "26.1")
        version("26.1.1", "26.1.1")
        version("26.1.2", "26.1.2")
        version("26.2", "26.2")
        vcsVersion = "26.2"
    }
}

rootProject.name = "LitematicaMatReplace"
