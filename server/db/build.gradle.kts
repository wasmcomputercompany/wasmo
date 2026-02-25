plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.sqldelight)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

dependencies {
  implementation(libs.commons.dbcp2)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(libs.postgresql)
  implementation(libs.sqldelight.jdbc.driver)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":common:tokens"))
  implementation(project(":server:identifiers"))
  implementation(project(":server:passkeys:api"))
  testImplementation(project(":common:testing"))
  testImplementation(project(":server:testing"))
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
