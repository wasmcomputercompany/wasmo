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
        implementation(libs.r2dbc.postgresql)
        implementation(libs.sqldelight.async.extensions)
        implementation(libs.vertx.postgresql)
        implementation(project(":apps:journal:api"))
        implementation(project(":apps:journal:db"))
        implementation(project(":support:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":support:okio-html"))
        implementation(project(":support:sqldelight-wasmo"))
        resources.srcDir(journalDotWasmo)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.coroutines.reactive)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":platform:testing"))
        implementation(project(":os:server:sql:api"))
        implementation(project(":os:server:sql:r2dbc"))
      }
    }
  }
}

dependencies {
  add("jsResources", project(":apps:journal:admin-web-app"))
}
