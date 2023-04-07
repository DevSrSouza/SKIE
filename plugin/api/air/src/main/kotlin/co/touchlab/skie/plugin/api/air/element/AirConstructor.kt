package co.touchlab.skie.plugin.api.air.element

import co.touchlab.skie.plugin.api.air.type.AirType
import co.touchlab.skie.plugin.api.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstructor(
    override val symbol: AirFunction.Symbol,
    override val annotations: List<AirConstantObject>,
    override val origin: AirOrigin,
    override val dispatchReceiverParameter: AirValueParameter?,
    override val extensionReceiverParameter: AirValueParameter?,
    override val valueParameters: List<AirValueParameter>,
    override val typeParameters: List<AirTypeParameter>,
    override val returnType: AirType,
    override val containedStatementSize: Int,
    val isExported: Boolean,
    val visibility: AirVisibility,
    val isExternal: Boolean,
    val isPrimary: Boolean,
    val isExpect: Boolean,
    val contextReceiverParametersCount: Int,
) : AirFunction {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstructor(this, data)
}