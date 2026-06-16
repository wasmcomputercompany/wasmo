rootProject.name = "brevity"

includeBuild("brevity-build")

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  versionCatalogs {
    create("libs") {
      from(files("../../gradle/libs.versions.toml"))
    }
  }
}

include(":brevity-core")
include(":brevity-gradle-plugin")
include(":brevity-host")
include(":brevity-kotlin")
include(":brevity-kotlin-generator")
include(":brevity-testing")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
