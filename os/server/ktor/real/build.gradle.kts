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
        implementation(libs.kotlinx.io)
        implementation(libs.kotlinx.io.okio)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.server.call.logging)
        implementation(libs.ktor.server.core)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.logging)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.calls.wiring)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.ktor.api)
        implementation(projects.platform.api)
        implementation(projects.support.issues)
      }
    }
  }
}
