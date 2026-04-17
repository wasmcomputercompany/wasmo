package com.wasmo.domtester.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

class DomTesterPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add(
      DomTesterExtension::class.java,
      "domTester",
      RealDomTesterExtension(project),
    )
  }
}

internal class RealDomTesterExtension(
  private val project: Project,
) : DomTesterExtension {
  override fun domTester() {
    val writeSnapshotTestingJsTask = project.tasks.register(
      "writeKarmaConfigTask",
      WriteSnapshotTestingJsTask::class.java,
    ) {
      karmaConfigD.set(project.layout.projectDirectory.dir("karma.config.d"))
      fullyQualifiedProjectDirectory.set(project.projectDir.path)
      jvmResources.from(project.tasks.named { it == "jvmProcessResources" })
    }
    val cleanDomTesterSnapshotsTask = project.tasks.register("cleanDomTester", Delete::class.java) {
      delete(project.layout.projectDirectory.dir("dom-tester-snapshots"))
    }
    project.tasks.named { it == "jsBrowserTest" }.configureEach {
      dependsOn(writeSnapshotTestingJsTask)
      mustRunAfter(cleanDomTesterSnapshotsTask)
      outputs.dirs(
        project.layout.projectDirectory.dir("dom-tester-snapshots"),
        project.layout.buildDirectory.dir("dom-tester-snapshots"),
      )
    }
  }
}
