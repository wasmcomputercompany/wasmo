plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(libs.kotlinx.coroutines.reactive)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.okio.fakefilesystem)
        implementation(libs.okhttp)
        implementation(libs.vertx.postgresql)
        implementation(project(":os:server:downloader:real"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:objectstore:fs"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":os:server:sql:real"))
        implementation(project(":platform:api"))
      }
    }
  }
}
