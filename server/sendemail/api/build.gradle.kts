plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  js {
    browser()
  }
  jvm()
}
