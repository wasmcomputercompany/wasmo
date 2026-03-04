plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(project(":host:api"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:jobs:api"))
      }
      val jvmTest by getting {
        dependencies {
          implementation(libs.kotlinx.coroutines.test)
          implementation(project(":host:server:testing"))
          implementation(project(":platform:testing"))
        }
      }
    }
  }
}
