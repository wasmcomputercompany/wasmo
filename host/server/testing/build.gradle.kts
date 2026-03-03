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
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.okio.fakefilesystem)
        implementation(libs.postgresql)
        implementation(libs.sqldelight.jdbc.driver)
        implementation(libs.webauthn4j.core)
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:routes"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:accounts:real"))
        implementation(project(":host:server:calls:api"))
        implementation(project(":host:server:calls:real"))
        implementation(project(":host:server:computers"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:deployment"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:objectstore:api"))
        implementation(project(":host:server:passkeys:api"))
        implementation(project(":host:server:passkeys:real"))
        implementation(project(":host:server:payments:api"))
        implementation(project(":host:server:sendemail:api"))
        implementation(project(":host:server:website:api"))
        implementation(project(":host:server:website:real"))
        implementation(project(":host:testing"))
        implementation(project(":host:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":platform:testing"))
      }
    }
  }
}
