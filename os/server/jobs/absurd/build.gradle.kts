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
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.vertx.postgresql)
        implementation(libs.vertx.sql.client)
        implementation(projects.os.server.events.api)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.jobs.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.platform.api)
        implementation(projects.support.absurd)
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
