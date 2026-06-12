plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

dependencies {
  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlinpoet)
  implementation(libs.okio)
  implementation(projects.brevityCore)
  implementation(projects.brevityKotlinGenerator)
}

gradlePlugin {
  plugins {
    create("brevity") {
      id = "brevity"
      implementationClass = "dev.wasmo.brevity.gradle.BrevityPlugin"
    }
  }
}
