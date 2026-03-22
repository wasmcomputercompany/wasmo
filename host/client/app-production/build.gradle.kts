plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  applicationJs("wasmo", "jsBrowserDistribution")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":host:client:app"))
      }
    }
  }
}
