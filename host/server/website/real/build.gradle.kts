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
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:routes"))
        implementation(project(":host:server:accounts:api"))
        implementation(project(":host:server:calls:api"))
        implementation(project(":host:server:db"))
        implementation(project(":host:server:deployment"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:website:api"))
        implementation(project(":identifiers"))
        implementation(project(":platform:api"))
      }
    }
  }
}
