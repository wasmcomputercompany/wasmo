plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(project(":common:json"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
}
