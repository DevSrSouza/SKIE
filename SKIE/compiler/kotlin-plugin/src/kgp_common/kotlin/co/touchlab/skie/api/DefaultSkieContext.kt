package co.touchlab.skie.api

import co.touchlab.skie.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.util.directory.SkieDirectories

class DefaultSkieContext(
    override val module: SkieModule,
    override val skieConfiguration: SkieConfiguration,
    override val swiftCompilerConfiguration: SwiftCompilerConfiguration,
    override val skieDirectories: SkieDirectories,
    override val frameworkLayout: FrameworkLayout,
    override val analyticsCollector: AnalyticsCollector,
    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer,
    override val reporter: Reporter,
) : SkieContext
