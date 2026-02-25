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
        implementation(project(":host:json"))
        implementation(project(":host:tokens"))
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
      }
    }
  }
}
