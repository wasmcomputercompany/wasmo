plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvm()
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.okio)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.chicory.runtime)
      }
    }
  }
}
