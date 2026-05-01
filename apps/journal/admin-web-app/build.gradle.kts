plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.burst)
  id("wasmo-build")
  id("dom-tester")
}

wasmoBuild {
  applicationJs("admin", "jsBrowserDistribution")
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.core.js)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(projects.apps.journal.api)
        implementation(projects.support.router)
        implementation(projects.support.tokens)
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.support.domTester)
      }
    }
  }
}
