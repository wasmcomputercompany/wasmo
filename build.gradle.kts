import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
  id("wasmo-build").apply(false)
  id("dom-tester").apply(false)
  alias(libs.plugins.kotlin.jvm).apply(false)
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.compose.multiplatform).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.dokka).apply(false)
  alias(libs.plugins.ksp).apply(false)
  alias(libs.plugins.ktor).apply(false)
  alias(libs.plugins.maven.publish).apply(false)
  alias(libs.plugins.sqldelight).apply(false)
}

allprojects {
  // Generate a group name like 'com.wasmo.os.server.computers' for ':os:server:computers:api'.
  // Otherwise, publish coordinates collide: ':os:server:computers:api' yields 'com.wasmo:api'.
  group = "com.wasmo.${path.substring(0, path.lastIndexOf(':')).removePrefix(":").replace(':', '.')}"
  version = "0.1.0-SNAPSHOT"

  // Ensure jar file name derives from the whole gradle task path so that the various :real tasks don't conflict.
  val uniqueArchiveBaseName = path.removePrefix(":").replace(':', '-')
  tasks.withType<Jar>().configureEach {
    archiveBaseName.set(uniqueArchiveBaseName)
  }

  plugins.withType<KotlinMultiplatformPluginWrapper> {
    extensions.configure<KotlinMultiplatformExtension> {
      compilerOptions {
        optIn.add("kotlin.js.ExperimentalJsExport")
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }

    tasks.withType<KotlinJsCompile> {
      compilerOptions {
        target = "es2015"
      }
    }
  }

  plugins.withType<KotlinPluginWrapper> {
    extensions.configure<KotlinJvmExtension> {
      compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
      }
    }
  }

  plugins.withType<MavenPublishBasePlugin> {
    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = true)
      signAllPublications()
      pom {
        name = project.name
        description = "Your Cloud Computer"
        inceptionYear = "2026"
        url = "https://wasmo.com/"
        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id = "wasmo-team"
            name = "Wasmo Team"
            url = "https://wasmo.com/"
          }
        }
        scm {
          url = "https://github.com/wasmcomputercompany/wasmo/"
          connection = "scm:git:git://github.com/wasmcomputercompany/wasmo.git"
          developerConnection = "scm:git:ssh://git@github.com/wasmcomputercompany/wasmo.git"
        }
      }
    }
  }
}
