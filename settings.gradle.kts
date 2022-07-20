pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
            name = "gitHub-swiftpack"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("TL_READ_ACTOR") ?: githubActor
                password = System.getenv("TL_READ_TOKEN") ?: githubToken
            }
        }
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.github.com/Touchlab/SwiftPack") {
            name = "gitHub-swiftpack"
            credentials {
                val githubActor: String? by settings
                val githubToken: String? by settings
                username = System.getenv("TL_READ_ACTOR") ?: githubActor
                password = System.getenv("TL_READ_TOKEN") ?: githubToken
            }
        }
        mavenLocal()
    }
}

rootProject.name = "swiftkt"

include(":example")
include(":example:static")
include(":example:dynamic")

includeBuild("swiftkt-plugin")

