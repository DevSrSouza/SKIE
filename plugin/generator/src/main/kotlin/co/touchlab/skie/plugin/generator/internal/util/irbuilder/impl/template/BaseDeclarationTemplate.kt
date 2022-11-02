package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template

import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationTemplate
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.SyntheticDeclarationsGenerator

internal abstract class BaseDeclarationTemplate<D : DeclarationDescriptor, IR : IrDeclaration, S : IrBindableSymbol<D, IR>> :
    DeclarationTemplate<D> {

    override fun generateIr(parent: IrDeclarationContainer, generatorContext: GeneratorContext) {
        val syntheticDeclarationsGenerator = SyntheticDeclarationsGenerator(generatorContext)

        descriptor.accept(syntheticDeclarationsGenerator, parent)

        val symbol = getSymbol(generatorContext.symbolTable)
        val ir = symbol.owner

        val declarationIrBuilder = DeclarationIrBuilder(generatorContext, symbol, startOffset = 0, endOffset = 0)

        ir.patchDeclarationParents(ir.parent)
        ir.initialize(generatorContext.symbolTable, declarationIrBuilder)
    }

    protected abstract fun getSymbol(symbolTable: ReferenceSymbolTable): S

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) protected abstract fun IR.initialize() once possible
    protected abstract fun IR.initialize(symbolTable: ReferenceSymbolTable, declarationIrBuilder: DeclarationIrBuilder)
}