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
}

gradlePlugin {
  plugins {
    create("build-support") {
      id = "build-support"
      implementationClass = "com.wasmo.gradle.BuildSupport"
    }
  }
}
