plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  js {
    browser()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":common:api"))
        implementation(libs.okio)
      }
    }
  }
}
