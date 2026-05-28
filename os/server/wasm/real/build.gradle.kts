plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmWasm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.chicory.runtime)
        implementation(libs.chicory.wabt)
        implementation(libs.okhttp)
        implementation(libs.okio)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(projects.os.server.testing)
      }
    }
  }
}

// Required by RunKotlinWasmTest.
val compileDevelopmentExecutableKotlinWasmWasi = tasks.named("compileDevelopmentExecutableKotlinWasmWasi")
tasks.named("jvmTest") {
  dependsOn(compileDevelopmentExecutableKotlinWasmWasi)
}
