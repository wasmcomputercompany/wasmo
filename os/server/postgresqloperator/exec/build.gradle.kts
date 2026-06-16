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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.okio)
        implementation(projects.identifiers)
        implementation(projects.os.logging)
        implementation(projects.os.server.events.api)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.postgresqloperator.api)
        implementation(projects.os.server.sql.api)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.os.server.testing)
      }
    }
  }
}
