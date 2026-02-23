plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  js {
    browser()
  }
  jvm()

  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.okhttp)
      }
    }
  }
}
