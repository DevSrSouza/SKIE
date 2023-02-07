package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.api.model.callable.function.FakeObjcConstructorKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.getAllExposedMembers
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.util.mutableLazy
import co.touchlab.skie.util.swiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class ActualKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    private val namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
) : MutableKotlinClassSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.Class = ClassOrFileDescriptorHolder.Class(classDescriptor)

    override var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = allDirectlyCallableMembers.filterNot { it.visibility.isRemoved }

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel> by lazy {
        actualDirectlyCallableMembers + fakeObjcConstructors
    }

    private val actualDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = with(swiftModelScope) {
            descriptorProvider.getAllExposedMembers(classDescriptor)
                .map { it.swiftModel }
                .flatMap { it.directlyCallableMembers }
        }

    private val fakeObjcConstructors: List<MutableKotlinFunctionSwiftModel>
        get() = with(swiftModelScope) {
            classDescriptor.getSuperClassNotAny()
                ?.swiftModelOrNull
                ?.allDirectlyCallableMembers
                ?.flatMap { it.allBoundedSwiftModels }
                ?.filterIsInstance<FakeObjcConstructorKotlinFunctionSwiftModel>()
                ?.filter { it.receiver == this@ActualKotlinClassSwiftModel }
                ?: emptyList()
        }

    override val companionObject: MutableKotlinClassSwiftModel?
        get() = with(swiftModelScope) {
            descriptorProvider.getExposedCompanionObject(classDescriptor)?.swiftModel
        }

    override val enumEntries: List<KotlinEnumEntrySwiftModel>
        get() = with(swiftModelScope) {
            descriptorProvider.getExposedEnumEntries(classDescriptor).map { it.enumEntrySwiftModel }
        }

    override val nestedClasses: List<MutableKotlinClassSwiftModel>
        get() = with(swiftModelScope) {
            descriptorProvider.getExposedNestedClasses(classDescriptor).map { it.swiftModel }
        }

    private val swiftName = classDescriptor.swiftName

    override var identifier: String = swiftName.identifier

    override var bridge: TypeSwiftModel? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.Class

    override val objCFqName: String = namer.getClassOrProtocolName(classDescriptor.original).objCName

    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.Class(classDescriptor, namer)

    private val originalContainingType by lazy {
        with(swiftModelScope) {
            swiftName.containingClassName?.let { classDescriptor.getContainingClassNamed(it).swiftModel }
        }
    }

    override var containingType: MutableKotlinClassSwiftModel? by mutableLazy {
        originalContainingType
    }

    override val stableFqName: String =
        TypeSwiftModel.StableFqNameNamespace +
            ("class__${classDescriptor.module.swiftIdentifier}__${classDescriptor.fqNameSafe.asString()}").replace(".", "_")

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge

    override val original: KotlinClassSwiftModel = OriginalKotlinClassSwiftModel(
        delegate = this,
        // Needs to be recalculated because the `identifier` is changed if the class is nested inside a non-exported class.
        identifier = namer.getClassOrProtocolName(classDescriptor.original).swiftName.substringAfter("."),
        // Original containing type cannot point to non-exported class (it does not have a SwiftModel).
        containingType = lazy { originalContainingType },
    )

    private val ClassDescriptor.swiftName: SwiftName
        get() {
            val fullName = namer.getClassOrProtocolName(this.original).swiftName

            return if (fullName.contains(".")) this.getNestedClassSwiftName(fullName) else SwiftName(null, fullName)
        }

    private fun ClassDescriptor.getNestedClassSwiftName(fullName: String): SwiftName {
        val containingClassName = fullName.substringBefore(".")
        val identifier = fullName.substringAfter(".")

        val containingClass = this.getContainingClassNamed(containingClassName)

        return if (descriptorProvider.isExposed(containingClass)) {
            SwiftName(containingClassName, identifier)
        } else {
            val concatenatedIdentifier = containingClassName + identifier.replaceFirstChar(Char::uppercaseChar)

            SwiftName(null, concatenatedIdentifier)
        }
    }

    private fun ClassDescriptor.getContainingClassNamed(name: String): ClassDescriptor {
        val containingClass = this.containingDeclaration as ClassDescriptor

        val containingClassName = namer.getClassOrProtocolName(containingClass.original).swiftName

        return if (containingClassName == name) containingClass else containingClass.getContainingClassNamed(name)
    }

    private class SwiftName(val containingClassName: String?, val identifier: String)
}
