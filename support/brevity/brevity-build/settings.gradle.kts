rootProject.name = "brevity-build"

include(":brevity-gradle-plugin")
project(":brevity-gradle-plugin").projectDir = File("../brevity-gradle-plugin")
include(":brevity-core")
project(":brevity-core").projectDir = File("../brevity-core")
include(":brevity-kotlin")
project(":brevity-kotlin").projectDir = File("../brevity-kotlin")
include(":brevity-kotlin-generator")
project(":brevity-kotlin-generator").projectDir = File("../brevity-kotlin-generator")
include(":brevity-testing")
project(":brevity-testing").projectDir = File("../brevity-testing")

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
