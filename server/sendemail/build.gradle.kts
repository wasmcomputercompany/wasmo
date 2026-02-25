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
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(project(":server:sendemail:api"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":platform:testing"))
}
