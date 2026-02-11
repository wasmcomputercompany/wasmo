import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  js {
    browser {
      commonWebpackConfig {
        outputFileName = "wasmo.js"
      }
      webpackTask {
        this.output.library = "wasmo"
        this.output.libraryTarget = KotlinWebpackOutput.Target.VAR
      }
    }
    binaries.executable()
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":client:app"))
      }
    }
  }
}

val jsResources by configurations.creating {
  isCanBeResolved = false
  isCanBeConsumed = true
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, "jsResources"))
  }
  outgoing.artifact(
    tasks.named("jsBrowserDevelopmentExecutableDistribution").map {
      (it as Sync).destinationDir
    },
  )
}

val jsDevelopmentExecutableCompileSync by tasks.getting {}
val jsBrowserProductionWebpack by tasks.getting {
  dependsOn(jsDevelopmentExecutableCompileSync)
}
