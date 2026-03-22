plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  id("wasmo-build")
}

wasmoBuild {
  applicationJs("journal", "jsBrowserDistribution")
}

kotlin {
  sourceSets {
    val jsMain by getting {
      dependencies {
        compileOnly(libs.jetbrains.annotations)
        implementation(libs.compose.html)
        implementation(libs.compose.runtime)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.core.js)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(project(":apps:journal:api"))
      }
    }
  }
}
