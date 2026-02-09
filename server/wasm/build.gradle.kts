plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(libs.graalvm.polyglot)
  implementation(libs.graalvm.wasm)
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(project(":common:testing"))
  testImplementation(project(":server:testing"))
}
