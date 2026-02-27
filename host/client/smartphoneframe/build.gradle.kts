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
        compileOnly(libs.jetbrains.annotations)
        implementation(libs.compose.html)
        implementation(libs.compose.runtime)
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":dom-tester"))
      }
    }
  }
}
