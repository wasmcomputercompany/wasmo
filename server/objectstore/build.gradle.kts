plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(project(":server:objectstore:fs"))
        implementation(project(":server:objectstore:s3"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":platform:testing"))
      }
    }
  }
}
