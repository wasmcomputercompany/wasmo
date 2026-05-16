package com.wasmo.usernames
import com.wasmo.accounts.CallScope
import com.wasmo.api.CreateUsernameRequest
import com.wasmo.api.CreateUsernameResponse
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
@ClassKey(LinkUsernameRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class LinkUsernameRpc(
  private val usernameLinker: UsernameLinker,
) : RpcAction<LinkUsernameRequest, LinkUsernameResponse> {

  override suspend operator fun invoke(request: LinkUsernameRequest, url: Url) = handle(request)

  suspend fun handle(request: LinkUsernameRequest) =
    usernameLinker.linkExisting(request.username).let {
      Response(body = LinkUsernameResponse(it.decision, it.account))
    }
}

@Inject
@ClassKey(CreateUsernameRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateUsernameRpc(
  private val usernameLinker: UsernameLinker,
) : RpcAction<CreateUsernameRequest, CreateUsernameResponse> {

  override suspend fun invoke(request: CreateUsernameRequest, url: Url) = handle(request)

  suspend fun handle(request: CreateUsernameRequest) =
    usernameLinker.linkNew(request.username).let {
      Response(body = CreateUsernameResponse(it.decision, it.account))
    }
}

