plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":platform:api"))
  implementation(project(":server:objectstore:fs"))
  implementation(project(":server:objectstore:s3"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.okio.fakefilesystem)
  testImplementation(project(":platform:testing"))
}
