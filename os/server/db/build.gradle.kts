plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.sqldelight)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.commons.dbcp2)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.postgresql)
        implementation(libs.sqldelight.jdbc.driver)
        implementation(libs.tomlkt)
        implementation(project(":os:api"))
        implementation(project(":os:framework"))
        implementation(project(":os:tokens"))
        implementation(project(":os:server:identifiers"))
        implementation(project(":os:server:passkeys:api"))
        implementation(project(":os:server:sql:jdbc"))
        implementation(project(":identifiers"))
        implementation(project(":platform:packaging"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":os:server:testing"))
      }
    }
  }
}

sqldelight {
  databases {
    create("WasmoDb") {
      packageName.set("com.wasmo.db")
      dialect("app.cash.sqldelight:postgresql-dialect:${libs.versions.sqldelight.get()}")
      deriveSchemaFromMigrations.set(true)
      migrationOutputDirectory = layout.buildDirectory.dir("resources/main/migrations")
    }
  }
}
