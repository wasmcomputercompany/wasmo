plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
        implementation(projects.identifiers)
        implementation(projects.os.logging)
        implementation(projects.os.server.events.api)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.installedapps.api)
        implementation(projects.support.issues)
      }
    }
  }
}
