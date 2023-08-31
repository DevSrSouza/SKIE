package co.touchlab.skie.runtime.coroutines.suspend.internal

import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey

// Reimplementation of Kotlin_ObjCExport_WrapExceptionToNSError
internal fun Throwable.toNSError(): NSError {
    val userInfo = mutableMapOf<Any?, Any?>()

    userInfo["KotlinException"] = this
    userInfo["KotlinExceptionOrigin"] = ""

    if (message != null) {
        userInfo[NSLocalizedDescriptionKey] = message
    }

    return NSError(domain = "KotlinException", code = 0, userInfo = userInfo)
}
