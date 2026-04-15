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
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:calls:api"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:deployment"))
        implementation(project(":os:server:emails"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:passkeys:api"))
        implementation(project(":os:server:sendemail:api"))
        implementation(project(":os:server:website:api"))
        implementation(project(":os:server:website:real"))
        implementation(project(":platform:api"))
        implementation(project(":support:tokens"))
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
