package com.wasmo.testing.events

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.Event
import okio.ByteString

data class AfterInstallEvent(
  override val appSlug: AppSlug,
  val oldVersion: Long,
  val newVersion: Long,
) : Event

data class HandleJobEvent(
  override val appSlug: AppSlug,
  val queueName: String,
  val job: ByteString,
) : Event
