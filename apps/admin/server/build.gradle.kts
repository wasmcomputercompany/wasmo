plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.test)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":apps:admin:api"))
  implementation(project(":apps:admin:db"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  implementation(project(":platform:testing"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
}
