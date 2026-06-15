package dev.wasmo.brevity.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

@Suppress("unused") // Registered as a Gradle plugin.
class BrevityPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add(
      BrevityExtension::class.java,
      "brevity",
      RealBrevityExtension(project),
    )
  }
}

internal class RealBrevityExtension(
  val project: Project,
) : BrevityExtension {
  override fun generateKotlin(action: Action<BrevityTask>) {
    val brevityTask = project.tasks.register("brevity", BrevityTask::class.java) {
      group = "brevity"
      description = "generate Kotlin from WIT"
      outputKotlinCommonMain.value(project.layout.buildDirectory.dir("brevity/commonMain"))
      outputKotlinWasmWasiMain.value(project.layout.buildDirectory.dir("brevity/wasmWasiMain"))
      outputKotlinJvmMain.value(project.layout.buildDirectory.dir("brevity/jvmMain"))
      action.execute(this)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      val kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
      kotlin.apply {
        sourceSets.commonMain {
          generatedKotlin.srcDir(brevityTask.map { it.outputKotlinCommonMain })
        }
        sourceSets.wasmWasiMain {
          generatedKotlin.srcDir(brevityTask.map { it.outputKotlinWasmWasiMain })
        }
        sourceSets.jvmMain {
          generatedKotlin.srcDir(brevityTask.map { it.outputKotlinJvmMain })
        }
      }
    }
  }
}
