package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.type.translation.BuiltinSwiftBridgeableProvider
import co.touchlab.skie.api.model.type.translation.SwiftIrDeclarationRegistry
import co.touchlab.skie.api.model.type.translation.SwiftTranslationProblemCollector
import co.touchlab.skie.api.model.type.translation.SwiftTypeTranslator
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.CompilerOutputKind

class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    private val problemCollector: SwiftTranslationProblemCollector,
) {

    // TODO Refactor to phases
    fun process(configurables: AppleConfigurables, outputFile: String): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }
        val framework = FrameworkLayout(outputFile).also { it.cleanSkie() }
        val bridgeProvider = DescriptorBridgeProvider(namer)
        val swiftIrDeclarationRegistry = SwiftIrDeclarationRegistry(
            namer = namer,
        )
        val builtinSwiftBridgeableProvider = BuiltinSwiftBridgeableProvider(
            sdkPath = configurables.absoluteTargetSysRoot,
            declarationRegistry = swiftIrDeclarationRegistry,
        )

        val translator = SwiftTypeTranslator(
            descriptorProvider = descriptorProvider,
            namer = namer,
            problemCollector = problemCollector,
            builtinSwiftBridgeableProvider = builtinSwiftBridgeableProvider,
            swiftIrDeclarationRegistry = swiftIrDeclarationRegistry,
        )

        val swiftModelScope = DefaultSwiftModelScope(
            namer = namer,
            descriptorProvider = descriptorProvider,
            bridgeProvider = bridgeProvider,
            translator = translator,
            swiftIrDeclarationRegistry = swiftIrDeclarationRegistry,
        )

        SkieLinkingPhaseScheduler(
            skieContext = skieContext,
            skieModule = skieContext.module as DefaultSkieModule,
            descriptorProvider = descriptorProvider,
            framework = framework,
            swiftModelScope = swiftModelScope,
            swiftIrDeclarationRegistry = swiftIrDeclarationRegistry,
            configurables = configurables,
            config = config,
        ).runLinkingPhases()

        return skieContext.skieBuildDirectory.swiftCompiler.objectFiles.all.map { it.absolutePath }
    }
}
