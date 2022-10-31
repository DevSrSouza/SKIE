plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugin.kotlin.gradle)
    implementation(libs.plugin.kotlin.gradle.api)
    implementation(libs.plugin.pluginPublish)
    implementation(libs.plugin.buildconfig)
}

gradlePlugin {
    plugins.register("buildconfig") {
        id = "skie-buildconfig"
        implementationClass = "co.touchlab.skie.gradle.buildconfig.SkieBuildConfigPlugin"
    }
    plugins.register("jvm") {
        id = "skie-jvm"
        implementationClass = "co.touchlab.skie.gradle.kotlin.SkieKotlinJvmPlugin"
    }
    plugins.register("gradle-src-classpath-loader") {
        id = "gradle-src-classpath-loader"
        implementationClass = "co.touchlab.skie.gradle.loader.GradleSrcClasspathLoaderPlugin"
    }
    plugins.register("publish-jvm") {
        id = "skie-publish-jvm"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishJvmPlugin"
    }
    plugins.register("publish-multiplatform") {
        id = "skie-publish-multiplatform"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishMultiplatformPlugin"
    }
    plugins.register("publish-gradle") {
        id = "skie-publish-gradle"
        implementationClass = "co.touchlab.skie.gradle.publish.SkiePublishGradlePlugin"
    }
}

tasks.register("cleanAll") {
    dependsOn(allprojects.mapNotNull { it.tasks.findByName("clean") })
}
