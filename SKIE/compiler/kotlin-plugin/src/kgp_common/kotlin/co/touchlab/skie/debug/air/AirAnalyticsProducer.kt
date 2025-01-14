package co.touchlab.skie.debug.air

import co.touchlab.skie.debug.air.element.AirProject
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AirAnalyticsProducer(
    private val descriptorProvider: DescriptorProvider,
    private val modules: Collection<IrModuleFragment>,
) {

    private val json = Json { classDiscriminator = "jsonType" }

//     override fun produce(configuration: SkieFeature.Air): ByteArray =
//         getAir(configuration).serialize()

    private fun getAir(): AirProject =
        getFullAir()

    private fun getFullAir(): AirProject =
        IrToAirTransformer(descriptorProvider).transformToAir(modules)

    private fun AirProject.serialize(): ByteArray =
        json.encodeToString(this).toByteArray()
}
