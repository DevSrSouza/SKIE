package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class TopLevelFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : BaseFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exportedTopLevelCallableDescriptors
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty() && !this.belongsToSkieRuntime
}