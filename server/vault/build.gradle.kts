plugins {
  alias(libs.plugins.kotlin.jvm)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.okio)
}
