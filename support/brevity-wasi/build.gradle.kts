plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
  id("brevity")
}

wasmoBuild {
  libraryJvmWasm()
}

brevity {
  generateKotlin {
    inputWitPackageDirectories.from(
      File(project.rootDir, "submodules/WASI/proposals/cli/wit"),
      File(project.rootDir, "submodules/WASI/proposals/clocks/wit"),
      File(project.rootDir, "submodules/WASI/proposals/filesystem/wit"),
      File(project.rootDir, "submodules/WASI/proposals/http/wit"),
      File(project.rootDir, "submodules/WASI/proposals/random/wit"),
      File(project.rootDir, "submodules/WASI/proposals/sockets/wit"),
    )
  }
}

kotlin {
  sourceSets {
    commonMain {
      // Hack in a dependency on the runtime library because includeBuild isn't working?
      kotlin.srcDir("../brevity/brevity-kotlin/src/commonMain/kotlin")
      dependencies {
        api(libs.kotlinx.coroutines.core)
      }
    }
  }
}
