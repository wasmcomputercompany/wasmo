rootProject.name = "brevity-build"

include(":brevity")
project(":brevity").projectDir = File("../brevity")
include(":brevity-gradle-plugin")
project(":brevity-gradle-plugin").projectDir = File("../brevity-gradle-plugin")
include(":brevity-kotlin-generator")
project(":brevity-kotlin-generator").projectDir = File("../brevity-kotlin-generator")
include(":brevity-testing")
project(":brevity-testing").projectDir = File("../brevity-testing")
include(":brevity-wit")
project(":brevity-wit").projectDir = File("../brevity-wit")

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
      from(files("../../../gradle/libs.versions.toml"))
    }
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
