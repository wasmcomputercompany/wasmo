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
        api(libs.chicory.runtime)
      }
    }
  }
}
