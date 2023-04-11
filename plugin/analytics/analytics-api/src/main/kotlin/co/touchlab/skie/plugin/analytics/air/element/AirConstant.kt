package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirConstant : AirElement {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstant
}
