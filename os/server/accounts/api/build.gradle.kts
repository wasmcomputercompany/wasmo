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
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
      }
    }
  }
}
