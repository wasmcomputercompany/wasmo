plugins {
  alias(libs.plugins.kotlin.jvm)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":apps:hello:api"))
  implementation(project(":apps:hello:db"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":platform:testing"))
}
