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
        implementation(libs.okio)
        implementation(projects.apps.journal.api)
        implementation(projects.platform.api)
        implementation(projects.wasmox.wasmoxSqldelight)
      }
    }
  }
}

sqldelight {
  databases {
    create("JournalDb") {
      packageName.set("com.wasmo.journal.db")
      dialect("app.cash.sqldelight:postgresql-dialect:${libs.versions.sqldelight.get()}")
      deriveSchemaFromMigrations.set(true)
      generateAsync.set(true)
    }
  }
}
