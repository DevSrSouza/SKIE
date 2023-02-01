package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

interface KotlinCallableMemberSwiftModel {

    val descriptor: CallableMemberDescriptor

    val original: KotlinCallableMemberSwiftModel

    val receiver: TypeSwiftModel

    val origin: Origin

    val scope: Scope
        get() = if (origin in listOf(Origin.Global, Origin.Extension.Interface)) Scope.Static else Scope.Member

    val allBoundedSwiftModels: List<KotlinCallableMemberSwiftModel>

    val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT

    sealed interface Origin {

        sealed interface FromEnum : Origin

        object Global : Origin

        sealed interface Member : Origin {

            object Class : Member

            object Enum : Member, FromEnum

            object Interface : Member
        }

        sealed interface Extension : Origin {

            object Class : Extension

            object Enum : Extension, FromEnum

            object Interface : Extension
        }
    }

    enum class Scope {
        Static, Member
    }
}
