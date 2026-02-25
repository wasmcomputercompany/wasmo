package com.wasmo.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class WasmoProjectPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val libs = project.extensions.getByName("libs") as LibrariesForLibs

    project.extensions.add(
      WasmoBuildExtension::class.java,
      "wasmoBuild",
      RealWasmoBuildExtension(
        project = project,
        libs = libs,
      ),
    )
  }
}

internal class RealWasmoBuildExtension(
  private val project: Project,
  private val libs: LibrariesForLibs,
) : WasmoBuildExtension {
  override fun libraryJs() {
    libraryMultiplatform(js = true)
  }

  override fun libraryJvmJs() {
    libraryMultiplatform(jvm = true, js = true)
  }

  override fun libraryJvm() {
    libraryMultiplatform(jvm = true)
  }

  private fun libraryMultiplatform(
    jvm: Boolean = false,
    js: Boolean = false,
  ) {
    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      val kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
      kotlin.apply {
        kotlin.sourceSets.commonTest {
          dependencies {
            implementation(libs.assertk)
            implementation(libs.kotlin.test)
          }
        }

        if (js) {
          js {
            browser()
          }
          kotlin.sourceSets.jsTest {
            dependencies {
              implementation(libs.kotlin.test)
              implementation(libs.kotlin.test.js)
            }
          }
        }

        if (jvm) {
          jvm()
          kotlin.sourceSets.jvmTest {
            dependencies {
              implementation(libs.kotlin.test.junit)
            }
          }
        }
      }
    }
  }
}
