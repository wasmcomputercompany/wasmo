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
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
        implementation(projects.platform.api)
        implementation(projects.support.okioHtml)
      }
    }
  }
}
