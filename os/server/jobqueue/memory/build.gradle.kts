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
        implementation(libs.okio)
        implementation(libs.kotlinx.coroutines.core)
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:installedapps:api"))
        implementation(project(":os:server:jobqueue:api"))
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
