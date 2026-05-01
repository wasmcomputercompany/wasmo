plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(projects.identifiers)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.wasm.api)
        implementation(projects.platform.api)
        implementation(projects.platform.packaging)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(projects.os.server.testing)
      }
    }
  }
}
