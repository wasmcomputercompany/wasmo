plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

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
  implementation(project(":common:testing"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  implementation(project(":platform:filesystemobjectstore"))
  implementation(project(":platform:testing"))
  implementation(project(":server:actions"))
  implementation(project(":server:db"))
  implementation(project(":server:identifiers"))
}
