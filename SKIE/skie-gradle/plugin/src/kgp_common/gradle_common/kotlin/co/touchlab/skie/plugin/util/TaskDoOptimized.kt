@file:Suppress("ObjectLiteralToLambda")

package co.touchlab.skie.plugin.util

import org.gradle.api.Action
import org.gradle.api.Task

// We need to use an anonymous class instead of lambda to keep execution optimizations.
// https://docs.gradle.org/7.4.2/userguide/validation_problems.html#implementation_unknown
internal inline fun Task.doFirstOptimized(crossinline action: () -> Unit): Task =
    doFirst(
        object : Action<Task> {
            override fun execute(task: Task) {
                action()
            }
        },
    )

internal inline fun Task.doLastOptimized(crossinline action: () -> Unit): Task =
    doLast(
        object : Action<Task> {
            override fun execute(task: Task) {
                action()
            }
        },
    )
