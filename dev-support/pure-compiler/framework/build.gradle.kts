import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture

plugins {
    id("dev.multiplatform")
}

kotlin {
    ios()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    val exportedLibraries = listOf<String>(

    )

    targets.withType<KotlinNativeTarget> {
        binaries {
            framework {
                isStatic = true
                baseName = "Kotlin"
                freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=Kotlin")

                export(projects.devSupport.pureCompiler.dependency)

                exportedLibraries.forEach {
                    export(it)
                }
            }
        }
    }

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("co.touchlab.skie:configuration-annotations")
            implementation("co.touchlab.skie:runtime-kotlin")

            api(projects.devSupport.pureCompiler.dependency)

            exportedLibraries.forEach {
                api(it)
            }
        }
    }

    val iosMain by sourceSets.getting
    val iosTest by sourceSets.getting

    val iosSimulatorArm64Main by sourceSets.getting {
        dependsOn(iosMain)
    }
    val iosSimulatorArm64Test by sourceSets.getting {
        dependsOn(iosTest)
    }
}

tasks.withType<KotlinNativeLink>().configureEach {
    doLast {
        if (binary.target.konanTarget.family != org.jetbrains.kotlin.konan.target.Family.OSX) { return@doLast }
        val frameworkDirectory = outputs.files.toList().first()
        val apiFile = frameworkDirectory.resolve("KotlinApi.swift")
        exec {
            commandLine(
                "zsh",
                "-c",
                "echo \"import Kotlin\\n:type lookup Kotlin\" | swift repl -F \"${frameworkDirectory.absolutePath}\" > \"${apiFile.absolutePath}\"",
            )
        }
    }
}

tasks.register("dependenciesForExport") {
    doLast {
        val configuration = configurations.getByName(MacOsCpuArchitecture.getCurrent().kotlinGradleName + "Api")

        val dependencies = configuration.incoming.resolutionResult.allComponents.map { it.toString() }
        val externalDependencies = dependencies.filterNot { it.startsWith("project :") }

        externalDependencies.forEach {
            println("export(\"$it\")")
        }
    }
}
