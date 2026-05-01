plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.db)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.sql.api)
        implementation(projects.identifiers)
        implementation(projects.platform.api)
        implementation(projects.platform.packaging)
        implementation(projects.support.issues)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
  }
}
