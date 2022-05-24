pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "kotlin-gradle-plugin-template"

include(":example")
include(":example:static")
include(":example:dynamic")

includeBuild("plugin-build")
