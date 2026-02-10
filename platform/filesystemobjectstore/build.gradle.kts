plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okio)
  implementation(project(":platform:api"))
}
