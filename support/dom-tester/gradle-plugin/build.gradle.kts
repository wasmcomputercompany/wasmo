plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

dependencies {
  add("compileOnly", kotlin("gradle-plugin"))
  add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.okio)
}

gradlePlugin {
  plugins {
    create("dom-tester") {
      id = "dom-tester"
      implementationClass = "com.wasmo.domtester.gradle.DomTesterPlugin"
    }
  }
}
