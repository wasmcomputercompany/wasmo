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
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(projects.os.json)
        implementation(projects.identifiers)
        implementation(projects.platform.packaging)
        implementation(projects.support.tokens)
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
      }
    }
  }
}
