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
        implementation(libs.okio)
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:identifiers"))
      }
    }
  }
}
