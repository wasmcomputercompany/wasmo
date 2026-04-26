plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmJs()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
        implementation(project(":os:server:sendemail:api"))
      }
    }
  }
}
