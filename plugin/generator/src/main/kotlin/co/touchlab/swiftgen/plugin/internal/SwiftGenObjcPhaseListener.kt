package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import co.touchlab.swiftpack.api.buildSwiftPackModule
import co.touchlab.swiftpack.api.skieContext
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SwiftGenObjcPhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        buildSwift { swiftFileBuilderFactory ->
            buildIr(context) { declarationBuilder ->
                val swiftGenScheduler = SwiftGenScheduler(
                    skieContext = context.skieContext,
                    declarationBuilder = declarationBuilder,
                    namespaceProvider = NamespaceProvider(context.skieContext.module),
                    configuration = context.pluginConfiguration,
                    reporter = Reporter(context.configuration),
                )

                val descriptorProvider = DescriptorProvider(context)

                swiftGenScheduler.process(descriptorProvider)
            }
        }
    }

    private val CommonBackendContext.pluginConfiguration: Configuration
        get() = configuration.get(ConfigurationKeys.swiftGenConfiguration, Configuration {})

    private fun buildSwift(action: (SwiftFileBuilderFactory) -> Unit) {
        buildSwiftPackModule("SwiftGen") {
            val swiftFileBuilderFactory = SwiftFileBuilderFactory(this)

            action(swiftFileBuilderFactory)

            swiftFileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }

    private fun buildIr(context: CommonBackendContext, action: (DeclarationBuilder) -> Unit) {
        val declarationBuilder = DeclarationBuilderImpl(context)

        SwiftGenCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.configuration)

        action(declarationBuilder)
    }
}