plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
}
