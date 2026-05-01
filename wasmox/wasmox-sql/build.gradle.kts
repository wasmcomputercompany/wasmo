plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmWasm()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(projects.platform.api)
      }
    }
  }
}
