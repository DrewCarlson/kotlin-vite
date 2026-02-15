package org.drewcarlson.kotlinvite.tasks

import org.drewcarlson.kotlinvite.internal.ViteProcessRunner
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Task to build the Vite production bundle from compiled Kotlin/JS browser output.
 */
public abstract class ViteDistTask @Inject constructor(
    execOperations: ExecOperations,
) : BaseViteTask(execOperations) {

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    init {
        group = "kotlin browser"
    }

    @TaskAction
    public fun doExecute() {
        val runner = ViteProcessRunner(
            execOperations = execOperations,
            configure = {
                workingDir(this@ViteDistTask.workingDir.asFile.get())
                executable(nodeExecutable.get())
                args(
                    viteScript.asFile.get().absolutePath,
                    "build",
                    "--config",
                    configFile.asFile.orNull?.absolutePath ?: "vite.config.js",
                    "--outDir",
                    outputDir.asFile.get(),
                    "--emptyOutDir",
                )

                environment(env.get())
            },
        )

        runner.start().waitForResult()
    }
}
