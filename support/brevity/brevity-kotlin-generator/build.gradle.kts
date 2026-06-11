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
        implementation(libs.kotlinpoet)
        implementation(libs.okio)
        implementation(projects.support.brevity.brevityCore)
      }
    }
    jvmTest {
      dependencies {
        implementation(projects.support.brevity.brevityTesting)
      }
    }
  }
}
