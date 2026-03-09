plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.graalvm.polyglot)
        implementation(libs.graalvm.wasm)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":host:server:testing"))
      }
    }
  }
}
