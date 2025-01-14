package co.touchlab.skie.api.apinotes.builder

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class ApiNotes(
    @SerialName("Name")
    val moduleName: String,
    @SerialName("Classes")
    val classes: List<ApiNotesType> = emptyList(),
    @SerialName("Protocols")
    val protocols: List<ApiNotesType> = emptyList(),
) {

    fun createApiNotesFileContent(): String = coder.encodeToString(serializer(), this)

    companion object {

        private val coder = Yaml(
            configuration = YamlConfiguration(
                encodeDefaults = false,
                strictMode = false,
                breakScalarsAt = Int.MAX_VALUE,
            )
        )

        operator fun invoke(file: File): ApiNotes = coder.decodeFromStream(serializer(), file.inputStream())
    }
}
