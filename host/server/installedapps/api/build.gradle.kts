plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
      }
    }
  }
}
