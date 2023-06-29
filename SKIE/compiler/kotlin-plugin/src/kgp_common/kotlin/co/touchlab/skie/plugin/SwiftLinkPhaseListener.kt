@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
//
// class SwiftLinkPhaseListener : PhaseListener {
//
//     override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJECT_FILES
//
//     override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
//         super.afterPhase(phaseConfig, phaserState, context)
//
//         if (context !is KonanContext) {
//             return
//         }
//
//         val config = context.config
//         val namer = context.objCExport.namer
//
//         val swiftObjectFiles = SwiftLinkCompilePhase(
//             config,
//             context,
//             namer,
//         ).process()
//
//         context.compilerOutput += swiftObjectFiles
//
//         logSkiePerformance(context.skieContext)
//     }
//
//     private fun logSkiePerformance(context: SkieContext) {
//         context.analyticsCollector.collect(context.skiePerformanceAnalyticsProducer)
//     }
// }
