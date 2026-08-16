import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint)
}

// Read outside `allprojects`: inside that block `this` is each subproject, which
// has no `libs` accessor of its own.
val ktlintVersion = libs.versions.ktlint

// Apply ktlint everywhere so `./gradlew ktlintCheck` / `ktlintFormat` from the
// root covers the root build scripts and every module. The actual style rules
// live in .editorconfig, which ktlint reads directly.
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        version.set(ktlintVersion)
        // ktlintCheck should fail the build on violations (CI relies on this).
        ignoreFailures.set(false)
        outputToConsole.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.HTML)
        }
        filter {
            // Never lint generated sources.
            exclude { it.file.path.contains("${File.separator}build${File.separator}") }
        }
    }
}
