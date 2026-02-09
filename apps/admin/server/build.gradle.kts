plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(project(":apps:admin:api"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
}
