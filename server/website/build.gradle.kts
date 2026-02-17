plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.html)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":platform:api"))
  implementation(project(":server:accounts:api"))
}
