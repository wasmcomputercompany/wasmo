package com.wasmo.gradle

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
}
