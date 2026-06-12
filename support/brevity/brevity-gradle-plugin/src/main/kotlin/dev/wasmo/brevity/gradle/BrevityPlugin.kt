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
    val commonMain = project.layout.buildDirectory.dir("brevity/commonMain")
    project.tasks.register("brevity", BrevityTask::class.java) {
      group = "brevity"
      description = "generate Kotlin from WIT"
      outputKotlinCommonMain.value(commonMain)
      action.execute(this)
    }

    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      val kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
      kotlin.apply {
        sourceSets.commonMain {
          @OptIn(ExperimentalKotlinGradlePluginApi::class)
          generatedKotlin.srcDir(commonMain)
        }
      }
    }
  }
}
