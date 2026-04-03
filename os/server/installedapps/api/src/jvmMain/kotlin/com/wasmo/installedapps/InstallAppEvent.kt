package com.wasmo.installedapps

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.Event
import com.wasmo.issues.Issue

data class InstallAppEvent(
  override val computerSlug: ComputerSlug,
  override val appSlug: AppSlug,
  override val issues: List<Issue> = listOf(),
) : Event
