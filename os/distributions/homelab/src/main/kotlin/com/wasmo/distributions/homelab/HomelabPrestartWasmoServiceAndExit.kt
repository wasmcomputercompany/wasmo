package com.wasmo.distributions.homelab

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
  val homelabDistribution = HomelabDistribution()
  homelabDistribution.prestartWasmoServiceAndExit(args)
}
