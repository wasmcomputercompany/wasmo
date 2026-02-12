plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.noarg)
  alias(libs.plugins.kotlin.serialization)
}

noArg {
  annotation("jakarta.xml.bind.annotation.XmlRootElement")
}

dependencies {
  implementation(libs.jaxb.api)
  implementation(libs.jaxb.implementation)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(libs.retrofit.converter.jaxb3)
  implementation(project(":platform:api"))
  testImplementation(libs.assertk)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(":platform:testing"))
}
