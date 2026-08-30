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
      File(project.projectDir, "src/wit/content-type"),
      File(project.projectDir, "src/wit/jobs"),
      File(project.projectDir, "src/wit/json"),
      File(project.projectDir, "src/wit/object-store"),
      File(project.projectDir, "src/wit/platform"),
      File(project.projectDir, "src/wit/sql"),
      File(project.projectDir, "src/wit/uuid"),
      File(project.rootDir, "submodules/wasi-p2/preview2/cli"),
      File(project.rootDir, "submodules/wasi-p2/preview2/clocks"),
      File(project.rootDir, "submodules/wasi-p2/preview2/filesystem"),
      File(project.rootDir, "submodules/wasi-p2/preview2/http"),
      File(project.rootDir, "submodules/wasi-p2/preview2/io"),
      File(project.rootDir, "submodules/wasi-p2/preview2/random"),
      File(project.rootDir, "submodules/wasi-p2/preview2/sockets"),
    )
  }
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(libs.brevity)
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
