rootProject.name = "dom-tester-gradle-plugin"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../../../gradle/libs.versions.toml"))
    }
  }
}
