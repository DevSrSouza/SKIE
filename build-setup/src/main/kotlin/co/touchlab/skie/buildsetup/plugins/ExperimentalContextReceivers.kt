package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class ExperimentalContextReceivers: Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        plugins.withType<KotlinMultiplatformPluginWrapper> {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        enableLanguageFeature("ContextReceivers")
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper> {
            tasks.withType<KotlinCompilationTask<*>>().configureEach {
                compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
            }
        }
    }
}
