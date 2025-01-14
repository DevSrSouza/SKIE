package co.touchlab.skie.debug.air.element

import co.touchlab.skie.debug.air.visitor.AirElementTransformer
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirDeclaration : AirElement {

    val annotations: List<AirConstantObject>

    val origin: AirOrigin

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirDeclaration
}
