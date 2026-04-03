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
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:objectstore:api"))
        implementation(project(":platform:api"))
      }
    }
  }
}
