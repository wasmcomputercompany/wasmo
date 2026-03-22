package com.wasmo.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

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

  override fun consumeJsResources(
    path: String,
  ) {
    val jsResources = project.configurations.create("jsResources") {
      isCanBeResolved = true
      isCanBeConsumed = false
      attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, "jsResources"))
      }
    }

    val copyJsResources = project.tasks.register("copyJsResources", Copy::class.java) {
      from(jsResources)
      into(project.layout.buildDirectory.dir("jsResources/$path"))
    }

    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer

    // If the project is multiplatform, the JVM source set is jvmMain.
    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      sourceSets.named("jvmMain").configure {
        resources.srcDir(copyJsResources.map { project.layout.buildDirectory.dir("jsResources") })
      }
    }
    // Otherwise it's main.
    project.plugins.withType<KotlinPluginWrapper> {
      sourceSets.named("main").configure {
        resources.srcDir(copyJsResources.map { project.layout.buildDirectory.dir("jsResources") })
      }
    }
  }

  override fun applicationJs(
    name: String,
    artifactTaskName: String,
  ) {
    val jsResources = project.configurations.create("jsResources") {
      isCanBeResolved = false
      isCanBeConsumed = true
      attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, "jsResources"))
      }
    }

    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      val kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
      kotlin.apply {
        js {
          browser {
            commonWebpackConfig {
              outputFileName = "$name.js"
            }
            webpackTask {
              this.output.library = name
              this.output.libraryTarget = KotlinWebpackOutput.Target.VAR
            }
          }
          binaries.executable()
        }
      }

      val jsBrowserProductionWebpack = project.tasks.named("jsBrowserProductionWebpack")
      jsBrowserProductionWebpack.configure {
        dependsOn(project.tasks.named("jsDevelopmentExecutableCompileSync"))
      }

      jsResources.outgoing.artifact(
        project.tasks.named(artifactTaskName).map { (it as Sync).destinationDir },
      )
    }
  }
}
