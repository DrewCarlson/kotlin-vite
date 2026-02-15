package org.drewcarlson.kotlinvite.internal

import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import javax.inject.Inject

internal open class ProcessDeploymentHandle @Inject constructor(
    private val runner: ViteProcessRunner,
) : DeploymentHandle {
    private var process: ExecAsyncHandle? = null

    override fun isRunning(): Boolean = process?.isAlive() == true

    override fun start(deployment: Deployment) {
        process = runner.start()
    }

    override fun stop() {
        process?.abort()
    }
}
