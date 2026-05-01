plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.webauthn4j.core)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.deployment)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.passkeys.api)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.platform.testing)
        implementation(projects.os.server.testing)
      }
    }
  }
}
