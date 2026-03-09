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
        implementation(project(":host:api"))
        implementation(project(":host:framework"))
        implementation(project(":host:tokens"))
        implementation(project(":host:server:identifiers"))
        implementation(project(":host:server:passkeys:api"))
        implementation(project(":identifiers"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(project(":host:server:testing"))
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
