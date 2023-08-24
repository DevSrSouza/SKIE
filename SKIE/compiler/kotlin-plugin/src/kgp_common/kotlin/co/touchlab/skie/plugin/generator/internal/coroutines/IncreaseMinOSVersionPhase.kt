package co.touchlab.skie.plugin.generator.internal.coroutines

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.properties.KonanPropertiesLoader

class IncreaseMinOSVersionPhase(
    private val configuration: SkieConfiguration,
    private val konanConfig: KonanConfig,
) : SkieCompilationPhase {

    override val isActive: Boolean
        get() = SkieConfigurationFlag.Feature_CoroutinesInterop in configuration.enabledConfigurationFlags

    private val coroutinesMinOsVersionMap = mutableMapOf(
        "osVersionMin.ios_arm32" to "13.0",
        "osVersionMin.ios_arm64" to "13.0",
        "osVersionMin.ios_simulator_arm64" to "13.0",
        "osVersionMin.ios_x64" to "13.0",
        "osVersionMin.macos_arm64" to "10.15",
        "osVersionMin.macos_x64" to "10.15",
        "osVersionMin.tvos_arm64" to "13.0",
        "osVersionMin.tvos_simulator_arm64" to "13.0",
        "osVersionMin.tvos_x64" to "13.0",
        "osVersionMin.watchos_arm32" to "6.0",
        "osVersionMin.watchos_arm64" to "6.0",
        "osVersionMin.watchos_device_arm64" to "6.0",
        "osVersionMin.watchos_simulator_arm64" to "6.0",
        "osVersionMin.watchos_x64" to "6.0",
        "osVersionMin.watchos_x86" to "6.0",
    )

    override fun runObjcPhase() {
        val properties = (konanConfig.platform.configurables as? KonanPropertiesLoader)?.properties ?: return

        coroutinesMinOsVersionMap.forEach { (key, requiredMinVersion) ->
            val currentMinVersion = properties.getProperty(key)

            val unifiedMinVersion = getHigherVersion(currentMinVersion, requiredMinVersion)

            properties.setProperty(key, unifiedMinVersion)
        }
    }

    private fun getHigherVersion(lhs: String, rhs: String): String =
        getHigherVersion(lhs.split("."), rhs.split(".")).joinToString(".")

    private fun getHigherVersion(lhs: List<String>, rhs: List<String>): List<String> =
        when {
            lhs.isEmpty() -> rhs
            rhs.isEmpty() -> lhs
            lhs.first().isHigherVersionComponentThan(rhs.first()) -> lhs
            rhs.first().isHigherVersionComponentThan(lhs.first()) -> rhs
            else -> lhs.take(1) + getHigherVersion(lhs.drop(1), rhs.drop(1))
        }

    private fun String.isHigherVersionComponentThan(other: String): Boolean {
        val lhsIntVersion = this.toIntOrNull()
        val rhsIntVersion = other.toIntOrNull()

        return when {
            lhsIntVersion != null && rhsIntVersion != null -> lhsIntVersion > rhsIntVersion
            lhsIntVersion != null -> true
            rhsIntVersion != null -> false
            else -> this > other
        }
    }
}