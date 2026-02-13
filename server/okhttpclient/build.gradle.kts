plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(libs.okhttp.coroutines)
  implementation(project(":platform:api"))
}
