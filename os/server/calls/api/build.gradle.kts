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
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.db)
        implementation(projects.os.server.deployment)
        implementation(projects.os.server.sql.api)
        implementation(projects.identifiers)
        implementation(projects.platform.api)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
  }
}
