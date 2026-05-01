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
        implementation(libs.okio)
        implementation(projects.os.server.db)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.permits.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.platform.api)
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
