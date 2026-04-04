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
        implementation(project(":identifiers"))
        implementation(project(":os:api"))
        implementation(project(":os:client:compose"))
        implementation(project(":os:client:framework"))
        implementation(project(":os:client:identifiers"))
        implementation(project(":os:client:passkeys:api"))
        implementation(project(":os:client:passkeys:real"))
        implementation(project(":os:client:smartphoneframe"))
        implementation(project(":os:framework"))
        implementation(project(":os:logging"))
        implementation(project(":os:routes"))
        implementation(project(":support:tokens"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":support:dom-tester"))
      }
    }
  }
}
