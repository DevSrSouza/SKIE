package co.touchlab.skie.plugin.api.model.type

interface MutableKotlinClassSwiftModel : KotlinClassSwiftModel, MutableKotlinTypeSwiftModel {

    override val companionObject: MutableKotlinClassSwiftModel?

    override val nestedClasses: List<MutableKotlinClassSwiftModel>
}
