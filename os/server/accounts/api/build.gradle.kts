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
        implementation(libs.okio)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.db)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.sql.api)
        implementation(projects.platform.api)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
  }
}
