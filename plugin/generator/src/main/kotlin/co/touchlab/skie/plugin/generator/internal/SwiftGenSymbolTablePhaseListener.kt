package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.PhaseListener
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SwiftGenSymbolTablePhaseListener : PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.CREATE_SYMBOL_TABLE

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        val descriptorProvider = DescriptorProvider(context)

        val declarationBuilder = DeclarationBuilderImpl(context, descriptorProvider)

        SwiftGenCompilerConfigurationKey.DescriptorProvider.put(descriptorProvider, context.configuration)
        SwiftGenCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.configuration)
    }
}
