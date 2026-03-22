plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":identifiers"))
      }
    }
  }
}
