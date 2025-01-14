@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.analytics.environment

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.target.Xcode

@Serializable
data class CompilerEnvironmentAnalytics(
    val jvmVersion: String,
    val compilerVersion: String?,
    val xcodeVersion: String,
    val availableProcessors: Int,
    val maxJvmMemory: Long,
) {

    class Producer(private val konanConfig: KonanConfig) : AnalyticsProducer {

        override val name: String = "compiler-environment"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_CompilerEnvironment

        override fun produce(): String =
            CompilerEnvironmentAnalytics(
                jvmVersion = Runtime.version().toString(),
                compilerVersion = konanConfig.distribution.compilerVersion,
                xcodeVersion = Xcode.findCurrent().version,
                availableProcessors = Runtime.getRuntime().availableProcessors(),
                maxJvmMemory = Runtime.getRuntime().maxMemory(),
            ).toPrettyJson()
    }
}
