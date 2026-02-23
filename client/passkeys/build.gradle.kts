plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  kotlin("plugin.js-plain-objects") version libs.versions.kotlin
}

kotlin {
  js {
    browser()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":common:api"))
        implementation(project(":client:passkeys:api"))
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
    val commonTest by getting {
      dependencies {
        implementation(libs.assertk)
        implementation(libs.kotlin.test)
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(libs.kotlin.test.js)
      }
    }
  }
}
