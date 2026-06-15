plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("wasmo-build")
  id("brevity")
}

wasmoBuild {
  libraryJvmWasm()
}

brevity {
  generateKotlin {
    inputWitPackageDirectories.from(
      File(projectDir, "src/commonMain/wit"),
    )
  }
}

kotlin {
  sourceSets {
    commonMain {
      // Hack in a dependency on the runtime library because includeBuild isn't working?
      kotlin.srcDir("../../../../support/brevity/brevity-kotlin/src/commonMain/kotlin/")
    }
    val jvmMain by getting {
      // Hack in a dependency on the runtime library because includeBuild isn't working?
      kotlin.srcDir("../../../../support/brevity/brevity-kotlin/src/jvmMain/kotlin/")
      dependencies {
        implementation(libs.chicory.runtime)
        implementation(libs.chicory.wabt)
        implementation(libs.okhttp)
        implementation(libs.okio)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
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
