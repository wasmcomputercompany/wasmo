plugins {
  alias(libs.plugins.kotlin.jvm)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(libs.graalvm.polyglot)
  implementation(libs.graalvm.wasm)
  testImplementation(project(":common:testing"))
  testImplementation(project(":server:testing"))
}
