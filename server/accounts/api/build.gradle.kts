plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okio)
  implementation(project(":common:api"))
  implementation(project(":server:db"))
  implementation(project(":server:identifiers"))
}
