package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.AnyTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftAnySirType : SwiftNonNullReferenceSirType {

    override val declaration = BuiltinDeclarations.Any

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName = AnyTypeName.INSTANCE

    override fun toString(): String = asString()
}
