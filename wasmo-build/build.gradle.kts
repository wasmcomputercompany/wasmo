plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
}

dependencies {
  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.okio)

  // So the plugin can see org.gradle.accessors.dm.LibrariesForLibs
  implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
  plugins {
    create("wasmo-build") {
      id = "wasmo-build"
      implementationClass = "com.wasmo.gradle.WasmoProjectPlugin"
    }
  }
}
