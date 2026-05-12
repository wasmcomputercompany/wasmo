package com.wasmo.accounts.usernames

import com.wasmo.accounts.CallScope
import com.wasmo.api.LinkUsernameRequest
import com.wasmo.api.LinkUsernameResponse
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@Inject
@ClassKey(CreateUsernameRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateUsernameRpc(
) : RpcAction<LinkUsernameRequest, LinkUsernameResponse> {
  override suspend fun invoke(
    request: LinkUsernameRequest,
    url: Url,
  ): Response<LinkUsernameResponse> {
    TODO("Not yet implemented")
  }
}
