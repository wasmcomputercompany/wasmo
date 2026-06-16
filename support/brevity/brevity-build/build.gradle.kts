import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
  `kotlin-dsl`
}

allprojects {
  plugins.withType<KotlinMultiplatformPluginWrapper> {
    extensions.configure<KotlinMultiplatformExtension> {
      compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }
  }

  plugins.withType<KotlinPluginWrapper> {
    extensions.configure<KotlinJvmExtension> {
      compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }
  }
}

