package co.touchlab.skie.plugin.analytics.air.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirStatementContainer {

    val containedStatementSize: Int
}