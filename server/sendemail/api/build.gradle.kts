plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.okio)
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":platform:testing"))
}
