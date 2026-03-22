plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJs()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":host:api"))
        implementation(libs.okio)
      }
    }
  }
}
