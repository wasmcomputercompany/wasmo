plugins {
  alias(libs.plugins.kotlin.multiplatform)
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
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio)
        implementation(libs.vertx.postgresql)
        implementation(projects.os.server.sql.api)
        implementation(projects.os.server.sql.real)
        implementation(projects.platform.api)
        implementation(projects.support.closeTracker)
      }
    }
  }
}
