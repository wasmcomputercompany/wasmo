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
        implementation(project(":platform:api"))
        implementation(project(":platform:sqldelight"))
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
