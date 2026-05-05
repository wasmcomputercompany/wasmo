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
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.routes)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.calls.api)
        implementation(projects.os.server.computers.api)
        implementation(projects.os.server.db)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.passkeys.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.identifiers)
        implementation(projects.platform.api)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
  }
}
