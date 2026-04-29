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
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
        implementation(project(":support:close-tracker"))
      }
    }
  }
}
