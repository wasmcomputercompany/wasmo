plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("build-support")
}

kotlin {
  js {
    browser()
  }

  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(npm("html2canvas", "1.4.1"))
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
