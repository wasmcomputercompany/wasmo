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
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:routes"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:calls:api"))
        implementation(project(":os:server:computers:api"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:deployment"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:website:api"))
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
        implementation(project(":support:okio-html"))
      }
    }
  }
}
