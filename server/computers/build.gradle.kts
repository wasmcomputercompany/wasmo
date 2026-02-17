plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(libs.postgresql)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":platform:api"))
  implementation(project(":server:db"))
  implementation(project(":server:apps"))
  implementation(project(":server:computers:api"))
  implementation(project(":server:downloader"))
  implementation(project(":server:identifiers"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":server:testing"))
}
