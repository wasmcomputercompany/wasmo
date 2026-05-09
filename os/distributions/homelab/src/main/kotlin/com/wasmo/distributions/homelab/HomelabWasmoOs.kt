@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("HomelabWasmoOs")

package com.wasmo.distributions.homelab

import kotlinx.coroutines.runBlocking

/**
 * Entrypoint for running Homelab Wasmo server.
 */
fun main(args: Array<String>): Unit = runBlocking {
  val homelabDistribution = HomelabDistribution()
  homelabDistribution.start(args)
}
