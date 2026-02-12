plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okio)
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
}
