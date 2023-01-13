package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.KotlinInterfaceExtensionPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.types.KotlinType

interface SwiftModelScope {

    val FunctionDescriptor.swiftModel: KotlinFunctionSwiftModel

    val ValueParameterDescriptor.swiftModel: KotlinParameterSwiftModel

    val PropertyDescriptor.swiftModel: KotlinPropertySwiftModel

    val PropertyDescriptor.regularPropertySwiftModel: KotlinRegularPropertySwiftModel

    val PropertyDescriptor.interfaceExtensionPropertySwiftModel: KotlinInterfaceExtensionPropertySwiftModel

    val ClassDescriptor.swiftModel: KotlinTypeSwiftModel

    val SourceFile.swiftModel: KotlinTypeSwiftModel

    val KotlinType.isBridged: Boolean
}
