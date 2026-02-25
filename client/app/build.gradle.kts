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
  libraryJvmJs()
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
        implementation(project(":client:compose"))
        implementation(project(":client:framework"))
        implementation(project(":client:passkeys:api"))
        implementation(project(":client:passkeys:real"))
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":common:logging"))
        implementation(project(":common:routes"))
        implementation(project(":common:tokens"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":common:testing"))
        implementation(project(":dom-tester"))
      }
    }
  }
}
