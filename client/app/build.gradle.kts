plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  js {
    browser()
    useEsModules()
  }

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
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":common:logging"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
        implementation(libs.kotlin.test.js)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":common:testing"))
      }
    }
  }
}
