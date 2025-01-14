package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

data class SwiftGenericTypeUsageSirType(
    override val declaration: SwiftIrTypeParameterDeclaration,
) : SwiftNonNullReferenceSirType {

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName = TypeVariableName(declaration.name)

    override fun toString(): String = asString()
}
