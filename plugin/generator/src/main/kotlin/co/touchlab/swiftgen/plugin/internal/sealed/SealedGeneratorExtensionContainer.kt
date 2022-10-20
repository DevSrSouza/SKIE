package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.configuration.ConfigurationContainer
import co.touchlab.swiftgen.plugin.internal.util.SwiftPoetExtensionContainer
import co.touchlab.swiftgen.plugin.internal.util.SwiftPoetExtensionContainer.Companion.TYPE_VARIABLE_BASE_BOUND_NAME
import co.touchlab.swiftgen.plugin.internal.util.isVisibleFromSwift
import co.touchlab.swiftpack.api.SwiftPoetContext
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

internal interface SealedGeneratorExtensionContainer : ConfigurationContainer, SwiftPoetExtensionContainer {

    val ClassDescriptor.elseCaseName: String
        get() = this.getConfiguration(ConfigurationKeys.SealedInterop.ElseName)

    val ClassDescriptor.enumCaseName: String
        get() {
            val configuredName = this.getConfiguration(ConfigurationKeys.SealedInterop.Case.Name)

            return configuredName ?: this.name.identifier
        }

    val ClassDescriptor.hasElseCase: Boolean
        get() = this.sealedSubclasses.any { !it.isVisibleSealedSubclass } || this.sealedSubclasses.isEmpty()

    val ClassDescriptor.visibleSealedSubclasses: List<ClassDescriptor>
        get() = this.sealedSubclasses.filter { it.isVisibleSealedSubclass }

    val ClassDescriptor.isVisibleSealedSubclass: Boolean
        get() {
            val isVisible = this.isVisibleFromSwift

            val isEnabled = this.getConfiguration(ConfigurationKeys.SealedInterop.Case.Visible)

            return isVisible && isEnabled
        }

    context(ClassDescriptor, SwiftPoetContext)
    fun swiftNameWithTypeParametersForSealedCase(parent: ClassDescriptor): TypeName {
        if (kind.isInterface) {
            return this@ClassDescriptor.spec
        }

        val typeParameters = declaredTypeParameters.map {
            val indexInParent = it.indexInParent(this@ClassDescriptor, parent)

            if (indexInParent != null) {
                parent.declaredTypeParameters[indexInParent].swiftName
            } else {
                TYPE_VARIABLE_BASE_BOUND_NAME
            }
        }

        return this@ClassDescriptor.spec.withTypeParameters(typeParameters)
    }

    private fun TypeParameterDescriptor.indexInParent(child: ClassDescriptor, parent: ClassDescriptor): Int? {
        if (parent.kind.isInterface) {
            return null
        }

        val parentType = child.typeConstructor.supertypes
            .firstOrNull { it.constructor.declarationDescriptor == parent }
            ?: throw IllegalArgumentException("$parent is not a parent of $this.")

        val index = parentType.arguments.indexOfFirst { it.type == this.defaultType }

        return if (index != -1) index else null
    }
}