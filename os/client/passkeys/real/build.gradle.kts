plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  kotlin("plugin.js-plain-objects") version libs.versions.kotlin
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJs()
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":os:api"))
        implementation(project(":os:client:identifiers"))
        implementation(project(":os:client:passkeys:api"))
        implementation(libs.okio)
        implementation(libs.kotlinx.serialization.json)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core.js)
        implementation(npm("@passwordless-id/webauthn", "2.3.1"))
      }
    }
  }
}
