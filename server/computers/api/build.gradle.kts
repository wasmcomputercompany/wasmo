plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(project(":common:api"))
  implementation(project(":platform:api"))
}
