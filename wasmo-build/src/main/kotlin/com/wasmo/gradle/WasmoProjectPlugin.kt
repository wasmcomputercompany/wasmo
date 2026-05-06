@file:OptIn(ExperimentalWasmDsl::class)

package com.wasmo.gradle

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SourcesJar
import io.freefair.gradle.plugins.sass.SassCompile
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

@Suppress("unused") // Used reflectively.
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
  override fun library(
    jvm: Boolean,
    js: Boolean,
    wasm: Boolean,
    publish: Boolean,
  ) {
    multiplatformConvention(jvm, js, wasm)

    if (publish) {
      project.plugins.apply(libs.plugins.maven.publish.get().pluginId)
      project.plugins.apply(libs.plugins.dokka.get().pluginId)

      project.plugins.withType<MavenPublishBasePlugin> {
        project.extensions.configure<MavenPublishBaseExtension> {
          configure(
            KotlinMultiplatform(
              JavadocJar.Dokka("dokkaGenerateHtml"),
              SourcesJar.Sources(),
            ),
          )
        }
      }
    }
  }

  private fun multiplatformConvention(
    jvm: Boolean = false,
    js: Boolean = false,
    wasm: Boolean = false,
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

        if (wasm) {
          wasmWasi {
            nodejs()
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

  override fun compileScss() {
    project.plugins.apply(libs.plugins.sass.get().pluginId)

    val cssResourcesDir = project.layout.buildDirectory.dir("sass")

    val compileScss = project.tasks.register("compileScss", SassCompile::class.java) {
      source(project.layout.projectDirectory.dir("src/main/scss"))
      includePaths.from(project.rootProject.layout.projectDirectory.dir("submodules/pico/scss"))
      destinationDir.set(cssResourcesDir)
    }

    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
    project.plugins.withType<KotlinMultiplatformPluginWrapper> {
      sourceSets.named("jvmMain").configure {
        resources.srcDir(compileScss)
      }
    }
  }

  override fun applicationJs(
    name: String,
    artifactTaskName: String,
  ) {
    multiplatformConvention(js = true)

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

  override fun createWasmoFileTask(slug: String): TaskProvider<WasmoFileTask> {
    val cliConfiguration = try {
      project.configurations.create("cliConfiguration") {
        isCanBeResolved = true
      }.also { cliConfiguration ->
        project.dependencies {
          cliConfiguration(project(":os:cli"))
        }
      }
    } catch (_: InvalidUserDataException) {
      // This configuration already exists.
      project.configurations.named("cliConfiguration")
    }

    return project.tasks.register("${slug}DotWasmo", WasmoFileTask::class) {
      classpath.setFrom(cliConfiguration)
      setSlug(slug)
    }
  }
}
