package co.touchlab.skie.plugin.api.sir.type

import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName

object SwiftMetaClassSirType : SwiftNonNullReferenceSirType {

    override val declaration = BuiltinDeclarations.AnyClass

    override val directChildren: List<SirType> = emptyList()

    override fun toSwiftPoetUsage(): TypeName = DeclaredTypeName.qualifiedTypeName("Swift.AnyClass")

    override fun toString(): String = asString()
}
