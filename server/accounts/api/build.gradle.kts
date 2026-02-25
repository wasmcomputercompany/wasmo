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
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":server:db"))
        implementation(project(":server:identifiers"))
      }
    }
  }
}
