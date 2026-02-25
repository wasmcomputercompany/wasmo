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
  implementation(project(":platform:api"))
  implementation(project(":server:objectstore:fs"))
  implementation(project(":server:objectstore:s3"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.okio.fakefilesystem)
  testImplementation(project(":platform:testing"))
}
