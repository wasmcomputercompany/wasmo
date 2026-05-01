plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.burst)
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
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.calls.api)
        implementation(projects.os.server.db)
        implementation(projects.os.server.deployment)
        implementation(projects.os.server.emails.messages)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.passkeys.api)
        implementation(projects.os.server.sendemail.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.os.server.website.api)
        implementation(projects.os.server.website.real)
        implementation(projects.platform.api)
        implementation(projects.support.tokens)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.platform.testing)
        implementation(projects.os.server.testing)
      }
    }
  }
}
