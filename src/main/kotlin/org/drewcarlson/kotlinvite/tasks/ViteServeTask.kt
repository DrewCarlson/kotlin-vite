package org.drewcarlson.kotlinvite.tasks

import org.drewcarlson.kotlinvite.internal.ProcessDeploymentHandle
import org.drewcarlson.kotlinvite.internal.ViteProcessRunner
import org.gradle.api.tasks.TaskAction
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.internal.extensions.core.serviceOf
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Task to run the Vite dev server against the compiled Kotlin/JS browser output.
 */
public abstract class ViteServeTask @Inject constructor(
    execOperations: ExecOperations,
) : BaseViteTask(execOperations) {

    private val isContinuous = project.gradle.startParameter.isContinuous

    init {
        group = "kotlin browser"
    }

    @TaskAction
    public fun doExecute() {
        val runner = ViteProcessRunner(
            execOperations = execOperations,
            configure = {
                workingDir(this@ViteServeTask.workingDir.asFile.get())
                executable(nodeExecutable.get())
                args(
                    viteScript.asFile.get().absolutePath,
                    "--config",
                    configFile.asFile.orNull?.absolutePath ?: "vite.config.js",
                )

                environment(env.get())
            },
        )

        if (isContinuous) {
            val deploymentRegistry = project.serviceOf<DeploymentRegistry>()
            val deploymentHandle = deploymentRegistry.get("vite", ProcessDeploymentHandle::class.java)
            if (deploymentHandle == null) {
                logger.lifecycle("Starting Vite server in continuous mode")
                deploymentRegistry.start(
                    "vite",
                    DeploymentRegistry.ChangeBehavior.BLOCK,
                    ProcessDeploymentHandle::class.java,
                    runner,
                )
            }
        } else {
            runner.start().waitForResult()
        }
    }
}
