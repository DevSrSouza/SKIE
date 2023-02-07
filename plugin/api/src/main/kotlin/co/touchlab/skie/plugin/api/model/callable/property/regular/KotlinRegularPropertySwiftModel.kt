package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

interface KotlinRegularPropertySwiftModel : KotlinPropertySwiftModel, KotlinDirectlyCallableMemberSwiftModel {

    override val original: KotlinRegularPropertySwiftModel

    val getter: KotlinRegularPropertyGetterSwiftModel

    val setter: KotlinRegularPropertySetterSwiftModel?

    val type: TypeSwiftModel

    val objCName: String
}
