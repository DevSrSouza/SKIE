package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.FunctionBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.DummyIrSimpleFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.symboltable.IrRebindableSimpleFunctionPublicSymbol
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.Name

internal class FunctionTemplate(
    name: Name,
    namespace: Namespace<*>,
    annotations: Annotations,
    config: FunctionBuilder.() -> Unit,
) : BaseDeclarationTemplate<FunctionDescriptor, IrSimpleFunction, IrSimpleFunctionSymbol>() {

    override val descriptor: SimpleFunctionDescriptorImpl = SimpleFunctionDescriptorImpl.create(
        namespace.descriptor,
        annotations,
        name,
        CallableMemberDescriptor.Kind.SYNTHESIZED,
        namespace.sourceElement,
    )

    private val functionBuilder = FunctionBuilder(descriptor)

    // TODO Change to context(IrPluginContext, DeclarationIrBuilder) once are context implemented properly
    private val irBodyBuilder: context(IrPluginContext) DeclarationIrBuilder.(IrSimpleFunction) -> IrBody

    init {
        functionBuilder.config()

        irBodyBuilder = functionBuilder.body ?: throw IllegalStateException("Function must have a body.")

        descriptor.initialize(
            functionBuilder.extensionReceiverParameter,
            functionBuilder.dispatchReceiverParameter,
            functionBuilder.contextReceiverParameters,
            functionBuilder.typeParameters,
            functionBuilder.valueParameters,
            functionBuilder.returnType,
            functionBuilder.modality,
            functionBuilder.visibility,
        )

        descriptor.isInline = functionBuilder.isInline
        descriptor.isSuspend = functionBuilder.isSuspend
    }

    override fun declareSymbol(symbolTable: SymbolTable) {
        val signature = symbolTable.signaturer.composeSignature(descriptor)
            ?: throw IllegalArgumentException("Only exported declarations are currently supported. Check declaration visibility.")

        // IrRebindableSimpleFunctionPublicSymbol is used so that we can later bind it to the correct declaration which cannot be created before the symbol table is validated to not contain any unbound symbols.
        val symbolFactory = { IrRebindableSimpleFunctionPublicSymbol(signature, descriptor) }
        val functionFactory = { symbol: IrSimpleFunctionSymbol ->
            DummyIrSimpleFunction(symbol).also {
                // In 1.8.0 the symbol is already present before calling declareSimpleFunction and therefore is not IrRebindableSimpleFunctionPublicSymbol
                // Starting from 1.9.0 the SymbolTable has additional check that requires that the symbol of created function is bounded in the factory.
                if (symbol is IrRebindableSimpleFunctionPublicSymbol) {
                    symbol.bind(it)
                }
            }
        }

        val declaration = symbolTable.declareSimpleFunction(signature, symbolFactory, functionFactory)
        // But the symbol cannot be bounded otherwise DeclarationBuilder will not to generate the declaration (because it thinks it already exists).
        (declaration.symbol as? IrRebindableSimpleFunctionPublicSymbol)?.unbind()
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun getSymbol(symbolTable: ReferenceSymbolTable): IrSimpleFunctionSymbol =
        symbolTable.referenceSimpleFunction(descriptor)

    override fun initializeBody(
        declaration: IrSimpleFunction,
        irPluginContext: IrPluginContext,
        declarationIrBuilder: DeclarationIrBuilder,
    ) {
        declaration.body = irBodyBuilder(irPluginContext, declarationIrBuilder, declaration)
    }
}
