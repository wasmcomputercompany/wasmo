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
        implementation(projects.identifiers)
        implementation(projects.os.api)
        implementation(projects.os.client.compose)
        implementation(projects.os.client.framework)
        implementation(projects.os.client.identifiers)
        implementation(projects.os.client.passkeys.api)
        implementation(projects.os.client.passkeys.real)
        implementation(projects.os.client.smartphoneframe)
        implementation(projects.os.framework)
        implementation(projects.os.logging)
        implementation(projects.os.routes)
        implementation(projects.support.tokens)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(projects.os.client.style)
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
