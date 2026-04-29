plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.burst)
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
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.vertx.postgresql)
        implementation(project(":os:server:events:api"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:jobs:api"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
        implementation(project(":support:absurd"))
        implementation(project(":support:tokens"))
        implementation(project(":wasmox:wasmox-sql"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":platform:testing"))
        implementation(project(":os:server:testing"))
      }
    }
  }
}
