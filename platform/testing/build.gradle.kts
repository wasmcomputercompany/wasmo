plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(libs.okio.fakefilesystem)
  implementation(libs.okhttp)
  implementation(project(":server:objectstore:fs"))
  implementation(project(":platform:api"))
}
