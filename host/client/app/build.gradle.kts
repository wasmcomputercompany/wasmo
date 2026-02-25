plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.burst)
  alias(libs.plugins.metro)
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
        implementation(project(":host:api"))
        implementation(project(":host:client:compose"))
        implementation(project(":host:client:framework"))
        implementation(project(":host:client:passkeys:api"))
        implementation(project(":host:client:passkeys:real"))
        implementation(project(":host:framework"))
        implementation(project(":host:logging"))
        implementation(project(":host:routes"))
        implementation(project(":host:tokens"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":host:testing"))
        implementation(project(":dom-tester"))
      }
    }
  }
}
