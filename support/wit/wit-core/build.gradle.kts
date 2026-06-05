plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.okio)
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}
