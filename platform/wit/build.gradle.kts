plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.brevity)
  id("wasmo-build")
}

wasmoBuild {
  library(
    jvm = true,
    wasm = true,
    publish = true,
  )
}

brevity {
  generateKotlin {
    worlds.add("wasmo:platform/wasmo")
    inputWitPackageDirectories.from(
      File(project.projectDir, "src/wit"),
    )
  }
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(libs.brevity)
        implementation(libs.brevity.wasi.p2)
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
