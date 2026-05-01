plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
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
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.tomlkt)
        implementation(projects.identifiers)
        implementation(projects.os.api)
        implementation(projects.os.framework)
        implementation(projects.os.server.accounts.api)
        implementation(projects.os.server.calls.api)
        implementation(projects.os.server.computers.api)
        implementation(projects.os.server.db)
        implementation(projects.os.server.deployment)
        implementation(projects.os.server.downloader.real)
        implementation(projects.os.server.events.api)
        implementation(projects.os.server.identifiers)
        implementation(projects.os.server.installedapps.api)
        implementation(projects.os.server.jobs.api)
        implementation(projects.os.server.payments.api)
        implementation(projects.os.server.sql.api)
        implementation(projects.os.server.wasm.api)
        implementation(projects.platform.api)
        implementation(projects.platform.packaging)
        implementation(projects.support.closeTracker)
        implementation(projects.support.issues)
        implementation(projects.wasmox.wasmoxSql)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.okio.fakefilesystem)
        implementation(projects.platform.testing)
        implementation(projects.os.server.testing)
        implementation(projects.os.server.website.api)
        implementation(projects.os.server.website.real)
      }
    }
  }
}
