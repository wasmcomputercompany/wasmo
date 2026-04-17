plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:deployment"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:emails:messages"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:sendemail:api"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.burst.coroutines)
        implementation(project(":platform:testing"))
        implementation(project(":os:server:testing"))
      }
    }
  }
}
