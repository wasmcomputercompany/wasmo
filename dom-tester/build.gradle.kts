plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.burst)
  id("wasmo-build")
  id("dom-tester")
}

wasmoBuild {
  libraryJs()
}

domTester {
  domTester()
}

kotlin {
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.compose.html)
        implementation(libs.compose.runtime)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(npm("html2canvas", "1.4.1"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":host:testing"))
      }
    }
  }
}
