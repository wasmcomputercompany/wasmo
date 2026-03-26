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
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":identifiers"))
        implementation(project(":platform:issues"))
        implementation(project(":platform:packaging"))
      }
    }
  }
}
