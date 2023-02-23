package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.api.apinotes.builder.ApiNotes
import co.touchlab.skie.plugin.Command
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassTypeModel
import org.jetbrains.kotlin.name.FqName
import java.io.File

class BuiltinSwiftBridgeableProvider(
    private val sdkPath: String,
) {
    val builtinBridges: Map<FqName, SwiftClassTypeModel> by lazy {
        getAllBuiltinBridges()
    }

    fun bridgeFor(fqName: FqName): SwiftClassTypeModel? = builtinBridges[fqName]

    private fun getAllBuiltinBridges(): Map<FqName, SwiftClassTypeModel> {
        val apiNotesFiles = getAllApiNotesFiles()
        return apiNotesFiles
            .map { file ->
                val apiNotes = ApiNotes(file)
                val moduleName = apiNotes.moduleName
                // TODO: Protocols can't really have bridges, do we want to keep them here?
                val allTypes = apiNotes.classes + apiNotes.protocols

                allTypes
                    .mapNotNull { apiNotesType ->
                        apiNotesType.bridgeFqName?.let { bridgeFqName ->
                            getKotlinFqName(moduleName, apiNotesType.objCFqName) to getSwiftModelFor(getSwiftFqName(moduleName, bridgeFqName))
                        }
                    }
            }
            .flatten()
            .toMap()
    }

    private fun getAllApiNotesFiles(): List<File> {
        val grepResult = Command(
            "grep",
            "--include=*.apinotes",
            "--recursive", "--files-with-matches", "--word-regexp",
            sdkPath.trimEnd('/') + '/',
            "--regexp=SwiftBridge:",
        ).execute()

        return grepResult.outputLines
            .map {
                File(it)
            }
            .filter {
                it.exists() && it.isFile
            }
    }

    private fun getSwiftModelFor(swiftFqName: String): SwiftClassTypeModel {
        return SwiftClassTypeModel(
            className = swiftFqName,
            typeArguments = when (swiftFqName) {
                "Swift.Array" -> listOf(SwiftAnyTypeModel)
                "Swift.Dictionary" -> listOf(SwiftAnyHashableTypeModel, SwiftAnyTypeModel)
                "Swift.Set" -> listOf(SwiftAnyHashableTypeModel)
                else -> emptyList()
            }
        )
    }

    private fun getSwiftFqName(module: String, bridgeFqName: String): String {
        val bridgeModule = bridgeFqName.substringBefore(".", missingDelimiterValue = "").ifBlank { module }
        val bridgeName = bridgeFqName.substringAfter(".")
        return "$bridgeModule.$bridgeName"
    }

    private fun getKotlinFqName(module: String, type: String): FqName = FqName("platform.${module}.${type}")
}