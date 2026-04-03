package com.wasmo.testing.events

import com.wasmo.events.Event
import com.wasmo.identifiers.AppSlug
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
