plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.sqldelight)
}

dependencies {
  implementation(libs.commons.dbcp2)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(libs.postgresql)
  implementation(libs.sqdelight.jdbc.driver)
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":common:tokens"))
  implementation(project(":server:identifiers"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(project(":common:testing"))
  testImplementation(project(":server:testing"))
}

sqldelight {
  databases {
    create("WasmComputerDb") {
      packageName.set("com.publicobject.wasmcomputer")
      dialect("app.cash.sqldelight:postgresql-dialect:${libs.versions.sqldelight.get()}")
      deriveSchemaFromMigrations.set(true)
      migrationOutputDirectory = layout.buildDirectory.dir("resources/main/migrations")
    }
  }
}
