plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":apps:hello:api"))
  implementation(project(":apps:hello:db"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":platform:testing"))
}
