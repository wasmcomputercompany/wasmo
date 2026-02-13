package com.wasmo.gradle

import com.wasmo.gradle.domtester.WriteSnapshotTestingJsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSupport : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add(
      WasmoBuildExtension::class.java,
      "wasmoBuild",
      RealWasmoBuildExtension(project),
    )
  }
}

internal class RealWasmoBuildExtension(
  private val project: Project,
) : WasmoBuildExtension {

  override fun domTester() {
    val writeSnapshotTestingJsTask = project.tasks.register(
      "writeKarmaConfigTask",
      WriteSnapshotTestingJsTask::class.java,
    ) {
      karmaConfigD.set(project.layout.projectDirectory.dir("karma.config.d"))
      fullyQualifiedProjectDirectory.set(project.projectDir.path)
    }
    project.tasks.named { it == "jsBrowserTest" }.configureEach {
      dependsOn(writeSnapshotTestingJsTask)
    }
  }
}
