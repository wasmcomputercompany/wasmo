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
        implementation(projects.identifiers)
        implementation(projects.os.server.identifiers)
        implementation(projects.support.issues)
      }
    }
  }
}
