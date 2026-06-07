plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmWasm()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
      }
    }
  }
}
