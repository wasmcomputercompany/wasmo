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
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":platform:api"))
        implementation(project(":server:accounts:api"))
        implementation(project(":server:db"))
        implementation(project(":server:deployment"))
        implementation(project(":server:downloader"))
        implementation(project(":server:identifiers"))
        implementation(project(":server:payments:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(project(":platform:testing"))
        implementation(project(":server:testing"))
      }
    }
  }
}
