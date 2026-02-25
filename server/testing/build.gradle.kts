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
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":common:routes"))
        implementation(project(":common:testing"))
        implementation(project(":common:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":platform:testing"))
        implementation(project(":server:accounts:api"))
        implementation(project(":server:accounts:real"))
        implementation(project(":server:computers"))
        implementation(project(":server:db"))
        implementation(project(":server:deployment"))
        implementation(project(":server:identifiers"))
        implementation(project(":server:objectstore:api"))
        implementation(project(":server:passkeys"))
        implementation(project(":server:passkeys:api"))
        implementation(project(":server:payments:api"))
        implementation(project(":server:sendemail:api"))
        implementation(project(":server:website"))
        implementation(project(":server:website:api"))
      }
    }
  }
}
