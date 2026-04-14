plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.burst)
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
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.sqldelight.runtime)
        implementation(libs.tomlkt)
        implementation(project(":identifiers"))
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:logging"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:calls:api"))
        implementation(project(":os:server:computers:api"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:deployment"))
        implementation(project(":os:server:downloader:real"))
        implementation(project(":os:server:events:api"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:installedapps:api"))
        implementation(project(":os:server:jobs:api"))
        implementation(project(":os:server:payments:api"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
        implementation(project(":support:issues"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":os:server:testing"))
        implementation(project(":os:server:website:api"))
        implementation(project(":os:server:website:real"))
        implementation(project(":platform:testing"))
      }
    }
  }
}
