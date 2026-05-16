package com.wasmo.usernames

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.UsernameDecision
import com.wasmo.identifiers.UsernameSlug

abstract class UsernameLinker {

  suspend fun linkNew(username: UsernameSlug) = link(username, Mode.LinkNew)
  suspend fun linkExisting(username: UsernameSlug) = link(username, Mode.LinkExisting)

  protected abstract suspend fun link(username: UsernameSlug, mode: Mode): UsernameLinkResult

  data class UsernameLinkResult(val decision: UsernameDecision, val account: AccountSnapshot?)

  protected enum class Mode {
    LinkExisting,
    LinkNew,
  }
}
