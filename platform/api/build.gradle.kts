plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmWasm()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.tomlkt)
      }
    }
  }
}
