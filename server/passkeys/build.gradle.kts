plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(libs.webauthn4j.core)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":server:accounts:api"))
  implementation(project(":server:db"))
  implementation(project(":server:identifiers"))
  implementation(project(":server:passkeys:api"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":common:testing"))
  testImplementation(project(":platform:testing"))
  testImplementation(project(":server:testing"))
}
