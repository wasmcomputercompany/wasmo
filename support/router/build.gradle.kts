plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  id("wasmo-build")
}

wasmoBuild {
  libraryJs()
}

kotlin {
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(libs.compose.html)
        implementation(libs.compose.runtime)
      }
    }
  }
}
