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
    val commonMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(libs.kotlinx.serialization.json)
        implementation(project(":platform:issues"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(libs.okhttp)
        implementation(libs.tomlkt)
      }
    }
  }
}
