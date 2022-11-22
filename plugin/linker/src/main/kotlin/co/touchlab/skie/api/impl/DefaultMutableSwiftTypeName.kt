package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.type.MutableSwiftTypeName
import co.touchlab.skie.plugin.api.type.SwiftTypeName

class DefaultMutableSwiftTypeName(
    private val originalParent: MutableSwiftTypeName?,
    private val originalIsNestedInParent: Boolean,
    override val originalSimpleName: String,
) : MutableSwiftTypeName {
    override var parent: MutableSwiftTypeName? = originalParent
    override var isNestedInParent: Boolean = originalIsNestedInParent
    override var simpleName = originalSimpleName

    override val isChanged: Boolean
        get() = isSimpleNameChanged || separator != originalSeparator || parent != originalParent || parent?.isChanged == true

    override val isSimpleNameChanged: Boolean
        get() = simpleName != originalSimpleName

    override val originalQualifiedName: String
        get() {
            val parentName = parent?.originalQualifiedName ?: return originalSimpleName
            return "$parentName$originalSeparator$originalSimpleName"
        }

    override val qualifiedName: String
        get() = qualifiedNameWithSeparators(SwiftTypeName.DEFAULT_SEPARATOR)

    private val originalSeparator: String
        get() = if (originalIsNestedInParent) SwiftTypeName.DEFAULT_SEPARATOR else ""
    private val separator: String
        get() = if (isNestedInParent) SwiftTypeName.DEFAULT_SEPARATOR else ""

    override fun qualifiedNameWithSeparators(separator: String): String {
        val parentName = parent?.qualifiedNameWithSeparators(separator) ?: return simpleName
        return "$parentName${this.separator}$simpleName"
    }
}