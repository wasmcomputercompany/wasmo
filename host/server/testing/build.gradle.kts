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
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.okio.fakefilesystem)
        implementation(libs.postgresql)
        implementation(libs.sqldelight.jdbc.driver)
        implementation(libs.tomlkt)
        implementation(libs.webauthn4j.core)
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:routes"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:accounts:real"))
        implementation(project(":host:server:calls:api"))
        implementation(project(":host:server:calls:real"))
        implementation(project(":host:server:computers:api"))
        implementation(project(":host:server:computers:real"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:deployment"))
        implementation(project(":host:server:events:api"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:installedapps:api"))
        implementation(project(":host:server:installedapps:real"))
        implementation(project(":host:server:jobs:api"))
        implementation(project(":host:server:jobs:memory"))
        implementation(project(":host:server:objectstore:api"))
        implementation(project(":host:server:objectstore:fs"))
        implementation(project(":host:server:passkeys:api"))
        implementation(project(":host:server:passkeys:real"))
        implementation(project(":host:server:payments:api"))
        implementation(project(":host:server:sendemail:api"))
        implementation(project(":host:server:website:api"))
        implementation(project(":host:server:website:real"))
        implementation(project(":host:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":identifiers"))
        implementation(project(":platform:packaging"))
        implementation(project(":platform:testing"))
      }
    }
  }
}
