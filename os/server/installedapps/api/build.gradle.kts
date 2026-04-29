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
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
        implementation(project(":support:issues"))
        implementation(project(":support:wasmox-sql"))
      }
    }
  }
}
