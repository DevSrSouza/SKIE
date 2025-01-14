package co.touchlab.skie.plugin.generator.internal.validation.rules

import co.touchlab.skie.configuration.Validation
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.plugin.generator.internal.configuration.getConfiguration
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal interface ClassBaseValidationRule : BaseValidationRule<ClassDescriptor> {

    context(SkieConfiguration) override fun severity(descriptor: ClassDescriptor): Reporter.Severity =
        descriptor.getConfiguration(Validation.Severity).asReporterSeverity
}
