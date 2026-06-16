plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
  jvm()
  wasmWasi()
  sourceSets {
    commonMain {
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
