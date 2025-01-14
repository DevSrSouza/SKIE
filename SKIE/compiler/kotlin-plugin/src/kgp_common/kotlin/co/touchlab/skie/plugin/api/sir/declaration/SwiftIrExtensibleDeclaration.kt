package co.touchlab.skie.plugin.api.sir.declaration

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.sir.SwiftFqName

sealed interface SwiftIrExtensibleDeclaration : SwiftIrDeclaration {

    val superTypes: List<SwiftIrExtensibleDeclaration>

    val swiftGenericExportScope: SwiftGenericExportScope

    // TODO: Remove
    // val fqName: String
    //     get() = toInternalSwiftPoetName().toString()

    // val swiftFqName: SwiftFqName

    val internalName: SwiftFqName

    val publicName: SwiftFqName

    // fun toInternalSwiftPoetName(): DeclaredTypeName
    //
    // fun toPublicSwiftPoetName(): DeclaredTypeName

    sealed interface Local : SwiftIrExtensibleDeclaration {

        val typealiasName: String

        override val publicName: SwiftFqName.Local
    }
}

// TODO: This is a bad implementation of `isHashable`. We need to be able to look into the hierarchy of the type
fun SwiftIrExtensibleDeclaration.isHashable(): Boolean {
    return this == BuiltinDeclarations.Swift.Hashable || this.superTypes.any { it.isHashable() }
}
