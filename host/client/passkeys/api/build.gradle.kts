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
        implementation(project(":host:api"))
        implementation(libs.okio)
      }
    }
  }
}
