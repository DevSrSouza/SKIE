@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridge
import co.touchlab.skie.plugin.api.model.type.bridge.NativeTypeBridge
import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.isHashable
import co.touchlab.skie.plugin.api.sir.type.ObjcProtocolSirType
import co.touchlab.skie.plugin.api.sir.type.SirType
import co.touchlab.skie.plugin.api.sir.type.SkieLambdaErrorSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnyHashableSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnyObjectSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftAnySirType
import co.touchlab.skie.plugin.api.sir.type.SwiftClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftErrorSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftInstanceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftLambdaSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftMetaClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftNonNullReferenceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftNullableReferenceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftPointerSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftPrimitiveSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftProtocolSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftReferenceSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftVoidSirType
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isInlined
import org.jetbrains.kotlin.backend.konan.isKotlinObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.checker.intersectWrappedTypes
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.hasTypeParameterRecursiveBounds
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections
import org.jetbrains.kotlin.types.typeUtil.supertypes

fun SirType.makeNullableIfReferenceOrPointer(): SirType = when (this) {
    is SwiftPointerSirType -> SwiftPointerSirType(pointee, nullable = true)
    is SwiftNonNullReferenceSirType -> SwiftNullableReferenceSirType(this)
    is SwiftNullableReferenceSirType, is SwiftPrimitiveSirType, SwiftVoidSirType, SwiftErrorSirType -> this
}

internal tailrec fun KotlinType.getErasedTypeClass(): Pair<KotlinType, ClassDescriptor> =
    TypeUtils.getClassDescriptor(this)?.let { this to it } ?: this.constructor.supertypes.first().getErasedTypeClass()

class SwiftTypeTranslator(
    private val descriptorProvider: DescriptorProvider,
    val namer: ObjCExportNamer,
    val problemCollector: SwiftTranslationProblemCollector,
    val builtinSwiftBridgeableProvider: BuiltinSwiftBridgeableProvider,
    val swiftIrDeclarationRegistry: SwiftIrDeclarationRegistry,
) {

    val builtinKotlinDeclarations: BuiltinDeclarations.Kotlin
        get() = swiftIrDeclarationRegistry.builtinKotlinDeclarations

    context(SwiftModelScope)
    internal fun mapFileType(sourceFile: SourceFile): SirType {
        val fileModel = sourceFile.swiftModel
        return SwiftClassSirType(fileModel.swiftIrDeclaration)
    }

    context(SwiftModelScope)
    internal fun mapReturnType(
        returnBridge: MethodBridge.ReturnValue,
        method: FunctionDescriptor,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType {
        return when (returnBridge) {
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void,
            -> SwiftVoidSirType
            MethodBridge.ReturnValue.HashCode -> SwiftPrimitiveSirType.NSUInteger
            is MethodBridge.ReturnValue.Mapped -> mapType(
                method.returnType!!,
                swiftExportScope,
                returnBridge.bridge,
                flowMappingStrategy,
            )
            MethodBridge.ReturnValue.WithError.Success -> SwiftVoidSirType
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(returnBridge.successBridge, method, swiftExportScope, flowMappingStrategy)

                if (!returnBridge.successMayBeZero) {
                    check(
                        successReturnType is SwiftNonNullReferenceSirType
                                || (successReturnType is SwiftPointerSirType && !successReturnType.nullable),
                    ) {
                        "Unexpected return type: $successReturnType in $method"
                    }
                }

                successReturnType.makeNullableIfReferenceOrPointer()
            }
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> SwiftInstanceSirType
        }
    }

    context(SwiftModelScope)
    internal fun mapReferenceType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftReferenceSirType =
        mapReferenceTypeIgnoringNullability(kotlinType, swiftExportScope, flowMappingStrategy).withNullabilityOf(kotlinType)

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullability(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftNonNullReferenceSirType {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: CustomTypeMapper)

        if (flowMappingStrategy == FlowMappingStrategy.Full) {
            val flowMapper = FlowTypeMappers.getMapperOrNull(kotlinType)
            if (flowMapper != null) {
                return flowMapper.mapType(kotlinType, this, swiftExportScope, flowMappingStrategy)
            }
        }

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                CustomTypeMappers.getMapper(descriptor)?.let { mapper ->
                    TypeMappingMatch(type, descriptor, mapper)
                }
            }
        }

        val mostSpecificMatches = typeMappingMatches.filter { match ->
            typeMappingMatches.all { otherMatch ->
                otherMatch.descriptor == match.descriptor || !otherMatch.descriptor.isSubclassOf(match.descriptor)
            }
        }

        if (mostSpecificMatches.size > 1) {
            val types = mostSpecificMatches.map { it.type }
            val firstType = types[0]
            val secondType = types[1]

            problemCollector.reportWarning(
                "Exposed type '$kotlinType' is '$firstType' and '$secondType' at the same time. " +
                        "This most likely wouldn't work as expected.",
            )
        }

        mostSpecificMatches.firstOrNull()?.let {
            return it.mapper.mapType(it.type, this, swiftExportScope, flowMappingStrategy)
        }

        return mapReferenceTypeIgnoringNullabilitySkippingPredefined(kotlinType, swiftExportScope, flowMappingStrategy)
    }

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullabilitySkippingPredefined(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftNonNullReferenceSirType {
        if (kotlinType.isTypeParameter()) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> return SwiftAnyHashableSirType
                else -> {
                    TypeUtils.getTypeParameterDescriptorOrNull(kotlinType)?.let { typeParameterDescriptor ->
                        val genericTypeUsage = swiftExportScope.genericScope.getGenericTypeUsage(typeParameterDescriptor)
                        if (genericTypeUsage != null) {
                            return genericTypeUsage
                        } else if (hasTypeParameterRecursiveBounds(typeParameterDescriptor)) {
                            val erasedType = intersectWrappedTypes(
                                kotlinType.immediateSupertypes().map {
                                    /* The commented out code below keeps more type information, but because that information is dropped by
                                        a probably missing functionallity in the Kotlin compiler, we have to erase it all to a star projection anyway. */
                                    it.replaceArgumentsWithStarProjections()
                                    // it.replace(newArguments = it.constructor.parameters.zip(it.arguments) { parameter, argument ->
                                    //     if (argument.type.constructor == kotlinType.constructor) {
                                    //         StarProjectionImpl(parameter)
                                    //     } else {
                                    //         argument
                                    //     }
                                    // })
                                },
                            )
                            return mapReferenceTypeIgnoringNullability(
                                erasedType,
                                swiftExportScope,
                                flowMappingStrategy.forTypeArgumentsOf(kotlinType),
                            )
                        } else if (kotlinType.immediateSupertypes().singleOrNull()?.isTypeParameter() == true) {
                            val referencedType = kotlinType.immediateSupertypes().single()
                            return mapReferenceTypeIgnoringNullability(
                                referencedType,
                                swiftExportScope,
                                flowMappingStrategy.forTypeArgumentsOf(referencedType),
                            )
                        }
                    }
                }
            }
        }

        val (kotlinType, classDescriptor) = kotlinType.getErasedTypeClass()

        if (KotlinBuiltIns.isAny(classDescriptor) || classDescriptor.classId in CustomTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
            return idType(swiftExportScope)
        }

        if (classDescriptor.defaultType.isObjCObjectType()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(classDescriptor, swiftExportScope)
        }

        if (classDescriptor !in descriptorProvider.exposedClasses) {
            return idType(swiftExportScope)
        }

        return if (classDescriptor.kind.isInterface) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableSirType
                else -> {
                    translateClassOrInterfaceName(
                        descriptor = classDescriptor,
                        exportScope = swiftExportScope,
                        typeArgs = { emptyList() },
                        ifKotlinType = { model, _ ->
                            SwiftProtocolSirType(model.nonBridgedDeclaration as SwiftIrProtocolDeclaration)
                        },
                    )
                }
            }
        } else {
            translateClassOrInterfaceName(
                descriptor = classDescriptor,
                exportScope = swiftExportScope,
                typeArgs = { typeParamScope ->
                    kotlinType.arguments.map { typeProjection ->
                        if (typeProjection.isStarProjection) {
                            idType(typeParamScope)
                        } else {
                            mapReferenceTypeIgnoringNullability(
                                typeProjection.type,
                                typeParamScope,
                                flowMappingStrategy.forTypeArgumentsOf(kotlinType),
                            )
                        }
                    }
                },
                ifKotlinType = { model, typeArgs ->
                    SwiftClassSirType(model.nonBridgedDeclaration, typeArgs)
                },
            )
        }
    }

    context(SwiftModelScope)
    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(
        descriptor: ClassDescriptor,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceSirType {
        if (descriptor.isObjCMetaClass()) return SwiftMetaClassSirType
        if (descriptor.isObjCProtocolClass()) return ObjcProtocolSirType

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            val bridge = builtinSwiftBridgeableProvider.bridgeFor(descriptor.fqNameSafe, swiftExportScope)
            return if (bridge != null) {
                bridge
            } else {
                val moduleName = "TODO: MODULE PLEASE"
                if (descriptor.kind.isInterface) {

                    // TODO("Get from registry")
                    // val name = SwiftFqName.External(
                    //     module = moduleName,
                    //     name = descriptor.name.asString().removeSuffix("Protocol"),
                    // )
                    // SwiftProtocolSirType(
                    //     SwiftIrProtocolDeclaration(
                    //         name,
                    //         superTypes = listOf(BuiltinSwiftDeclarations.nsObject),
                    //     ),
                    // )

                    SwiftProtocolSirType(
                        swiftIrDeclarationRegistry.declarationForInterface(descriptor)
                    )
                } else {
                    SwiftClassSirType(
                        swiftIrDeclarationRegistry.declarationForClass(descriptor)
                    )

                    // TODO("Get from registry")
                    // val name = SwiftFqName.External(moduleName, descriptor.name.asString())
                    // SwiftClassSirType(
                    //     SwiftIrTypeDeclaration(
                    //         name = name,
                    //         superTypes = listOf(BuiltinSwiftDeclarations.nsObject),
                    //     ),
                    // )
                }
            }
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny(), swiftExportScope)
        }

        return idType(swiftExportScope)
    }

    private fun idType(swiftExportScope: SwiftExportScope): SwiftNonNullReferenceSirType {
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableSirType
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftAnyObjectSirType
            else -> SwiftAnySirType
        }
    }

    context(SwiftModelScope)
    fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        swiftExportScope: SwiftExportScope,
        returnsVoid: Boolean,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftNonNullReferenceSirType {
        if (swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType)) {
            return SkieLambdaErrorSirType
        }

        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
                functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return SwiftLambdaSirType(
            if (returnsVoid) {
                SwiftVoidSirType
            } else {
                mapReferenceType(
                    functionType.getReturnTypeFromFunctionType(),
                    swiftExportScope.removingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forTypeArgumentsOf(functionType),
                )
            },
            parameterTypes.map {
                mapReferenceType(
                    it,
                    swiftExportScope.addingFlags(SwiftExportScope.Flags.Escaping),
                    flowMappingStrategy.forTypeArgumentsOf(functionType),
                )
            },
            isEscaping = swiftExportScope.hasFlag(SwiftExportScope.Flags.Escaping) && !functionType.binaryRepresentationIsNullable(),
        )
    }

    context(SwiftModelScope)
    private fun mapFunctionType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge.BlockPointer,
        flowMappingStrategy: FlowMappingStrategy,
    ): SwiftReferenceSirType {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType // Should not happen though.
        }

        return mapFunctionTypeIgnoringNullability(functionType, swiftExportScope, typeBridge.returnsVoid, flowMappingStrategy)
            .withNullabilityOf(kotlinType)
    }

    context(SwiftModelScope)
    internal fun mapType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge,
        flowMappingStrategy: FlowMappingStrategy,
    ): SirType = when (typeBridge) {
        NativeTypeBridge.Reference -> mapReferenceType(kotlinType, swiftExportScope, flowMappingStrategy)
        is NativeTypeBridge.BlockPointer -> mapFunctionType(kotlinType, swiftExportScope, typeBridge, flowMappingStrategy)
        is NativeTypeBridge.ValueType -> when (typeBridge.objCValueType) {
            ObjCValueType.BOOL -> SwiftPrimitiveSirType.Bool
            ObjCValueType.UNICHAR -> SwiftPrimitiveSirType.unichar
            ObjCValueType.CHAR -> SwiftPrimitiveSirType.Int8
            ObjCValueType.SHORT -> SwiftPrimitiveSirType.Int16
            ObjCValueType.INT -> SwiftPrimitiveSirType.Int32
            ObjCValueType.LONG_LONG -> SwiftPrimitiveSirType.Int64
            ObjCValueType.UNSIGNED_CHAR -> SwiftPrimitiveSirType.UInt8
            ObjCValueType.UNSIGNED_SHORT -> SwiftPrimitiveSirType.UInt16
            ObjCValueType.UNSIGNED_INT -> SwiftPrimitiveSirType.UInt32
            ObjCValueType.UNSIGNED_LONG_LONG -> SwiftPrimitiveSirType.UInt64
            ObjCValueType.FLOAT -> SwiftPrimitiveSirType.Float
            ObjCValueType.DOUBLE -> SwiftPrimitiveSirType.Double
            ObjCValueType.POINTER -> SwiftPointerSirType(SwiftVoidSirType, kotlinType.binaryRepresentationIsNullable())
        }
    }

    context(SwiftModelScope)
    private fun translateClassOrInterfaceName(
        descriptor: ClassDescriptor,
        exportScope: SwiftExportScope,
        typeArgs: (SwiftExportScope) -> List<SwiftNonNullReferenceSirType>,
        ifKotlinType: (model: KotlinTypeSwiftModel, typeArgs: List<SwiftNonNullReferenceSirType>) -> SwiftNonNullReferenceSirType,
    ): SwiftNonNullReferenceSirType {
        assert(descriptor in descriptorProvider.exposedClasses) { "Shouldn't be exposed: $descriptor" }

        if (ErrorUtils.isError(descriptor)) {
            return SwiftErrorSirType
        }

        fun swiftTypeArgs(): List<SwiftNonNullReferenceSirType> = typeArgs(exportScope)
        fun referenceTypeArgs(): List<SwiftNonNullReferenceSirType> =
            typeArgs(exportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType))

        return if (descriptor.hasSwiftModel) {
            val swiftModel = descriptor.swiftModel
            val bridge = swiftModel.bridge

            when {
                exportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> ifKotlinType(swiftModel, referenceTypeArgs())
                exportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> if (bridge != null && bridge.declaration.isHashable()) {
                    SwiftClassSirType(bridge.declaration, swiftTypeArgs())
                } else {
                    ifKotlinType(swiftModel, referenceTypeArgs())
                }
                else -> swiftModel.bridge?.let {
                    SwiftClassSirType(it.declaration, swiftTypeArgs())
                } ?: ifKotlinType(swiftModel, referenceTypeArgs())
            }
        } else {
            if (descriptor.kind.isInterface) {
                SwiftProtocolSirType(
                    swiftIrDeclarationRegistry.declarationForInterface(descriptor),
                )
            } else {
                SwiftClassSirType(
                    swiftIrDeclarationRegistry.declarationForClass(descriptor),
                    // TODO: We don't know if this is an ObjC or a Swift Type, so the type args are probably not correct.
                    referenceTypeArgs(),
                )
            }
        }
    }
}

fun SwiftNonNullReferenceSirType.withNullabilityOf(kotlinType: KotlinType): SwiftReferenceSirType =
    if (kotlinType.binaryRepresentationIsNullable()) SwiftNullableReferenceSirType(this) else this
