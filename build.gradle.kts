import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
  alias(libs.plugins.kotlin.jvm).apply(false)
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.compose.multiplatform).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.ksp).apply(false)
  alias(libs.plugins.ktor).apply(false)
  alias(libs.plugins.sqldelight).apply(false)
}

allprojects {
  plugins.withType<KotlinMultiplatformPluginWrapper> {
    extensions.configure<KotlinMultiplatformExtension> {
      compilerOptions {
        optIn.add("kotlin.js.ExperimentalJsExport")
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }

    tasks.withType<KotlinJsCompile> {
      compilerOptions {
        target = "es2015"
      }
    }
  }

  plugins.withType<KotlinPluginWrapper> {
    extensions.configure<KotlinJvmExtension> {
      compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }
  }
}
