plugins {
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.burst)
  id("brevity")
}

kotlin {
  jvm()

  @Suppress("OPT_IN_USAGE")
  wasmWasi {
    nodejs()
    binaries.executable()
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.brevityKotlin)
      }
    }
    jvmMain {
      dependencies {
        implementation(libs.okio)
        implementation(libs.chicory.runtime)
        implementation(libs.chicory.wabt)
        implementation(libs.okhttp)
        implementation(libs.okio)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.test.junit)
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}

brevity {
  generateKotlin {
    inputWitPackageDirectories.from(
      File(projectDir, "src/commonMain/wit"),
    )
  }
}

// Required by RunKotlinWasmTest.
val compileDevelopmentExecutableKotlinWasmWasi = tasks.named("compileDevelopmentExecutableKotlinWasmWasi")
tasks.named("jvmTest") {
  dependsOn(compileDevelopmentExecutableKotlinWasmWasi)
}
