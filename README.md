# kotlin-vite

[Vite](https://vite.dev/) support for Kotlin Multiplatform JS targets.

## Setup

Apply the plugin alongside the Kotlin Multiplatform plugin:

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.20"
    id("org.drewcarlson.kotlin-vite") version "<version>"
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
}
```

The plugin requires:
- Kotlin Multiplatform Gradle plugin
- At least one `js(IR)` target with `browser()` configured
- `binaries.executable()` declared on the JS target

## Tasks

For each JS browser target, the plugin registers the following tasks (shown here for a target named `js`):

| Task | Description |
|------|-------------|
| `jsBrowserDevelopmentServe` | Start the Vite dev server (development mode) |
| `jsBrowserProductionServe` | Start the Vite dev server (production mode) |
| `jsBrowserDevelopmentDist` | Build the Vite production bundle (development sources) |
| `jsBrowserProductionDist` | Build the Vite production bundle (production sources) |

All tasks automatically depend on `npmInstall` and the Kotlin/JS compilation link tasks.

### Continuous mode

The serve tasks integrate with Gradle's continuous build. Run with `--continuous` to enable hot-reload during development:

```shell
./gradlew jsBrowserDevelopmentServe --continuous
```

Gradle watches for source changes, recompiles Kotlin, and the Vite dev server picks up the updated output.

### Production builds

The dist tasks run `vite build` and write output to `build/vite/{targetName}/{binaryName}/`:

```shell
./gradlew jsBrowserProductionDist
```

## Configuration

Configure the plugin through the `kotlinVite` extension:

```kotlin
kotlinVite {
    // Vite version for the auto-installed npm dependency (default: "6.4.1")
    viteVersion = "6.4.1"

    // Automatically add Vite as a devNpm dependency (default: true)
    addViteDependency = true

    // Disable the default webpack/run/distribution tasks (default: true)
    disableWebpack = true

    // Disable the plugin entirely — no tasks are registered (default: true)
    enabled.set(true)

    // Path to a custom vite.config.js (default: projectDir/vite.config.js)
    configFilePath.set(file("vite.config.js"))

    // Environment variables passed to Vite
    environment["MY_VAR"] = "value"
}
```

### Options reference

| Property            | Type                         | Default                     | Description                                                |
|---------------------|------------------------------|-----------------------------|------------------------------------------------------------|
| `enabled`           | `Property<Boolean>`          | `true`                      | Master switch — when `false`, no tasks are registered      |
| `viteVersion`       | `String`                     | `"6.4.1"`                   | Vite npm package version added as a `devNpm` dependency    |
| `addViteDependency` | `Boolean`                    | `true`                      | Whether to automatically add the Vite npm dependency       |
| `disableWebpack`    | `Boolean`                    | `true`                      | Disable the redundant webpack, run, and distribution tasks |
| `configFilePath`    | `RegularFileProperty`        | `projectDir/vite.config.js` | Path to your Vite configuration file                       |
| `environment`       | `MutableMap<String, String>` | `{}`                        | Extra environment variables passed to Vite processes       |

### Disabling webpack tasks

By default, the plugin disables the webpack-based tasks that ship with Kotlin/JS since Vite replaces them. The following tasks are disabled per target:

- `{target}BrowserDevelopmentWebpack`
- `{target}BrowserProductionWebpack`
- `{target}BrowserDevelopmentRun`
- `{target}BrowserProductionRun`
- `{target}BrowserDistribution`

If you need both webpack and Vite tasks available, set `disableWebpack = false`:

```kotlin
kotlinVite {
    disableWebpack = false
}
```

### Custom Vite configuration

Place a `vite.config.js` in your project root (detected automatically), or point to a custom location:

```kotlin
kotlinVite {
    configFilePath.set(file("config/vite.config.js"))
}
```

```js
// vite.config.js
import { defineConfig } from "vite";

export default defineConfig({
    // your Vite configuration
});
```

### Managing Vite yourself

If you manage the Vite dependency through your own `package.json` or npm workspace, disable the automatic dependency:

```kotlin
kotlinVite {
    addViteDependency = false
}
```

## License

[MIT License](LICENSE)
