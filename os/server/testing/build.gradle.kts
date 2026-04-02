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
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.reactive)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.okio.fakefilesystem)
        implementation(libs.postgresql)
        implementation(libs.r2dbc.postgresql)
        implementation(libs.sqldelight.jdbc.driver)
        implementation(libs.tomlkt)
        implementation(libs.webauthn4j.core)
        implementation(project(":identifiers"))
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:routes"))
        implementation(project(":os:server:accounts:api"))
        implementation(project(":os:server:accounts:real"))
        implementation(project(":os:server:calls:api"))
        implementation(project(":os:server:calls:real"))
        implementation(project(":os:server:computers:api"))
        implementation(project(":os:server:computers:real"))
        implementation(project(":os:server:db"))
        implementation(project(":os:server:deployment"))
        implementation(project(":os:server:events:api"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:installedapps:api"))
        implementation(project(":os:server:installedapps:real"))
        implementation(project(":os:server:jobqueue:api"))
        implementation(project(":os:server:jobqueue:memory"))
        implementation(project(":os:server:jobs:api"))
        implementation(project(":os:server:jobs:memory"))
        implementation(project(":os:server:objectstore:api"))
        implementation(project(":os:server:objectstore:fs"))
        implementation(project(":os:server:passkeys:api"))
        implementation(project(":os:server:passkeys:real"))
        implementation(project(":os:server:payments:api"))
        implementation(project(":os:server:sendemail:api"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":os:server:sql:jdbc"))
        implementation(project(":os:server:sql:r2dbc"))
        implementation(project(":os:server:wasm:api"))
        implementation(project(":os:server:wasm:jvm"))
        implementation(project(":os:server:website:api"))
        implementation(project(":os:server:website:real"))
        implementation(project(":platform:api"))
        implementation(project(":platform:packaging"))
        implementation(project(":platform:testing"))
        implementation(project(":support:tokens"))
      }
    }
  }
}
