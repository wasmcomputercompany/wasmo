plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
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
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(projects.identifiers)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.db)
        implementation(projects.os.server.deployment)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.calls.api)
        implementation(projects.os.server.emails.messages)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.permits.api)
        implementation(projects.os.server.sendemail.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.platform.api)
        implementation(projects.support.tokens)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.burst.coroutines)
        implementation(projects.platform.testing)
        implementation(projects.os.server.testing)
      }
    }
  }
}
