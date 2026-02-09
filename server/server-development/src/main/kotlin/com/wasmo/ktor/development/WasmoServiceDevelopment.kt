@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServiceDevelopment")

package com.wasmo.ktor.development

import com.wasmo.ktor.WasmoService

fun main(args: Array<String>) {
  val service = WasmoService(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_development",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
  )
  service.start(args = args)
}
