package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.storage.MemoizedFunctionToNullable

class MapBasedMemoizedFunctionReflector<K, V : Any>(
    override val instance: MemoizedFunctionToNullable<K, V>,
) : Reflector("org.jetbrains.kotlin.storage.LockBasedStorageManager\$MapBasedMemoizedFunction") {

    val cache by declaredField<MutableMap<K, V>>()
}
