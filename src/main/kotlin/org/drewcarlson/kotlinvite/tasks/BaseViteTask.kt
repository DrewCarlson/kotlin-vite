package org.drewcarlson.kotlinvite.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecOperations

public abstract class BaseViteTask(
    @Internal
    protected val execOperations: ExecOperations,
) : DefaultTask() {

    @get:Input
    public abstract val nodeExecutable: Property<String>

    @get:InputFiles
    public abstract val viteScript: RegularFileProperty

    @get:Optional
    @get:InputFiles
    public abstract val configFile: RegularFileProperty

    @get:InputDirectory
    public abstract val workingDir: DirectoryProperty

    @get:Input
    public abstract val env: MapProperty<String, String>
}
