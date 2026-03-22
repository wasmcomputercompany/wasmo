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
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
      }
    }
  }
}
