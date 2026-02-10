plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.html)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(libs.postgresql)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  implementation(project(":platform:filesystemobjectstore"))
  implementation(project(":server:db"))
  implementation(project(":server:identifiers"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":common:testing"))
  testImplementation(project(":platform:testing"))
  testImplementation(project(":server:testing"))
}
