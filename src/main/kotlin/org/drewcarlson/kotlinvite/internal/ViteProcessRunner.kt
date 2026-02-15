package org.drewcarlson.kotlinvite.internal

import org.drewcarlson.kotlinvite.internal.ExecAsyncHandle.Companion.execAsync
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

internal class ViteProcessRunner(
    private val execOperations: ExecOperations,
    private val configure: ExecSpec.() -> Unit,
) {
    fun start(): ExecAsyncHandle {
        val handle = execOperations.execAsync("Vite") { exec ->
            configure(exec)
        }
        handle.start()
        return handle
    }
}
