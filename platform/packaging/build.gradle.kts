plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvmJs()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(project(":identifiers"))
        implementation(project(":support:issues"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.tomlkt)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}
