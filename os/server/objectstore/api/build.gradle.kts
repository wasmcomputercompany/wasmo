plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":os:server:objectstore:fs"))
        implementation(project(":platform:testing"))
        implementation(project(":support:tokens"))
      }
    }
  }
}
