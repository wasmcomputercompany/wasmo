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
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":common:api"))
        implementation(project(":common:framework"))
        implementation(project(":platform:api"))
        implementation(project(":server:accounts:api"))
        implementation(project(":server:db"))
        implementation(project(":server:deployment"))
        implementation(project(":server:website:api"))
      }
    }
  }
}
