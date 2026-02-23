plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.html)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":common:framework"))
}
