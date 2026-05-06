package com.wasmo.jobs.absurd

import com.wasmo.jobs.JobProcessor
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class AbsurdBindings {
  @Binds
  internal abstract fun bindOsJobQueueFactory(real: AbsurdOsJobQueue.Factory): OsJobQueue.Factory

  @Binds
  internal abstract fun bindJobProcessor(real: AbsurdOsJobProcessor) : JobProcessor
}
