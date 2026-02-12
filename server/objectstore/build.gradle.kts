plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(project(":platform:api"))
  implementation(project(":platform:filesystemobjectstore"))
  implementation(project(":platform:s3objectstore"))
}
