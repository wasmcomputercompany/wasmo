package com.wasmo.domtester.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

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
    val cleanDomTesterSnapshotsTask = project.tasks.register("cleanDomTester", Delete::class.java) {
      delete(project.layout.projectDirectory.dir("dom-tester-snapshots"))
    }

    val domTesterResourcesDirectory = project.layout.buildDirectory.dir("dom-tester-resources")
    val copyDomTesterResourcesTask = registerCopyDomTesterResourcesTask(domTesterResourcesDirectory)

    val writeSnapshotTestingJsTask = project.tasks.register(
      "writeSnapshotTestingJs",
      WriteSnapshotTestingJsTask::class.java,
    ) {
      karmaConfigD.set(project.layout.projectDirectory.dir("karma.config.d"))
      fullyQualifiedProjectDirectory.set(project.projectDir.path)
      fullyQualifiedResourcesDirectory.set(domTesterResourcesDirectory.get().asFile.path)
    }

    project.tasks.named { it == "jsBrowserTest" }.configureEach {
      dependsOn(writeSnapshotTestingJsTask)
      dependsOn(copyDomTesterResourcesTask)
      mustRunAfter(cleanDomTesterSnapshotsTask)
      outputs.dirs(
        project.layout.projectDirectory.dir("dom-tester-snapshots"),
        project.layout.buildDirectory.dir("dom-tester-snapshots"),
      )
    }
  }

  private fun registerCopyDomTesterResourcesTask(
    domTesterResourcesDirectory: Provider<Directory>,
  ): TaskProvider<Copy> {
    return project.tasks.register(
      "copyDomTesterResources",
      Copy::class.java,
    ) {
      duplicatesStrategy = DuplicatesStrategy.EXCLUDE

      val jvmMain = project.kotlinExtension.sourceSets.findByName("jvmMain")
      if (jvmMain != null) {
        from(jvmMain.resources) {
          include("static/assets/**/*")
        }
      }

      val jvmMainRuntimeClasspath = project.configurations.findByName("jvmMainRuntimeClasspath")
      if (jvmMainRuntimeClasspath != null) {
        for (jarFile in jvmMainRuntimeClasspath.files) {
          from(project.zipTree(jarFile)) {
            include("static/assets/**/*")
          }
        }
      }

      into(domTesterResourcesDirectory)
    }
  }
}
