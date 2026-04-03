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
        implementation(project(":identifiers"))
        implementation(project(":os:logging"))
        implementation(project(":os:server:events:api"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":support:issues"))
      }
    }
  }
}
