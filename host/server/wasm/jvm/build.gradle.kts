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
        implementation(project(":host:server:wasm:api"))
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":host:server:testing"))
      }
    }
  }
}
