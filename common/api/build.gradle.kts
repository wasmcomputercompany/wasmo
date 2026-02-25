plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmJs()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(project(":common:json"))
        implementation(project(":common:tokens"))
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
      }
    }
  }
}
