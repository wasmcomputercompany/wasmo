plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
  consumeJsResources("static/journal/assets")
}

val journalDotWasmo = wasmoBuild.createWasmoFileTask("journal")
  .apply {
    // Force the JS artifacts to build. This is pretty clumsy because we also package the .js in the
    // server artifact, which it doesn't need.
    configure {
      dependsOn("copyJsResources")
    }
  }

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.sqldelight.async.extensions)
        implementation(libs.vertx.postgresql)
        implementation(projects.apps.journal.api)
        implementation(projects.apps.journal.db)
        implementation(projects.support.tokens)
        implementation(projects.platform.api)
        implementation(projects.support.okioHtml)
        implementation(projects.wasmox.wasmoxSqldelight)
        resources.srcDir(journalDotWasmo)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.reactive)
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.platform.testing)
        implementation(projects.os.server.sql.api)
        implementation(projects.os.server.sql.real)
      }
    }
  }
}

dependencies {
  add("jsResources", projects.apps.journal.adminWebApp)
}
