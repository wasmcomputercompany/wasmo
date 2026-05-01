plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}


kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.okio)
        implementation(libs.vertx.postgresql)
        implementation(projects.identifiers)
        implementation(projects.platform.api)
        implementation(projects.support.closeTracker)
      }
    }
  }
}
