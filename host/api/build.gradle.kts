plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
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
        implementation(project(":platform:packaging"))
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
      }
    }
  }
}
