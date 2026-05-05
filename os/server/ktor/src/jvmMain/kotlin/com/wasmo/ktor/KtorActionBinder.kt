package com.wasmo.ktor

import com.wasmo.api.WasmoJson
import com.wasmo.common.logging.Logger
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.framework.UserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.identifiers.Deployment
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.ktor.http.HttpMethod
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.host
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.HostRouteSelector
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.PathSegmentRegexRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.RoutingRequest
import io.ktor.server.routing.createRouteFromPath
import io.ktor.server.routing.host
import io.ktor.server.util.url
import io.ktor.utils.io.asSource
import kotlinx.io.okio.asOkioSource
import kotlinx.serialization.KSerializer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.buffer
import wasmo.http.Header

@AssistedInject
class KtorActionBinder(
  private val deployment: Deployment,
  private val factory: Factory,
  private val logger: Logger,
  @Assisted private val route: Route,
) : Binder {
  private val rootUrl: Url
    get() = deployment.baseUrl.toString().decodeUrl()

  private fun withRoute(route: Route) = factory.create(route)

  override fun <R, S> rpc(
    pattern: HttpRequestPattern,
    requestAdapter: KSerializer<R>,
    responseAdapter: KSerializer<S>,
    action: suspend (UserAgent, R, Url) -> Response<S>,
  ) {
    require(pattern.method == null) { "unexpected method on RPC" }

    val rpcPattern = pattern.copy(
      method = HttpMethod.Post.value,
    )

    withRoute(rpcPattern.asRoute()).handleInternal { userAgent, wasmoUrl, call ->
      val request = requestAdapter.decode(call.request)
      val response = action(userAgent, request, wasmoUrl)
      Response(
        status = response.status,
        headers = response.headers,
        contentType = response.contentType,
        body = ResponseBody { sink ->
          sink.writeUtf8(WasmoJson.encodeToString(responseAdapter, response.body))
        },
      )
    }
  }

  override fun httpAction(
    pattern: HttpRequestPattern,
    action: suspend (UserAgent, Url, Request) -> Response<ResponseBody>,
  ) {
    withRoute(pattern.asRoute()).handleInternal { userAgent, url, routingCall ->
      action(userAgent, url, routingCall.request.toRequest())
    }
  }

  private fun HttpRequestPattern.asRoute(): Route {
    var result = route
    val host = host
    if (host != null) {
      result = result.createChild(
        HostRouteSelector(
          hostList = listOf(),
          hostPatterns = listOf(host),
          portsList = listOf(),
        ),
      )
    }

    val path = path
    result = when {
      path != null -> result.createRouteFromPath(path)
      else -> result.createChild(PathSegmentRegexRouteSelector(Regex(("/.*"))))
    }

    val method = this.method
    if (method != null) {
      result = result.createChild(HttpMethodRouteSelector(HttpMethod.parse(method)))
    }

    return result
  }

  override fun staticResources(
    host: Regex,
    pathPrefix: String,
    basePackage: String,
  ) {
    route.host(host) {
      staticResources(pathPrefix, basePackage)
    }
  }

  private fun handleInternal(
    action: suspend (
      UserAgent, Url, RoutingCall,
    ) -> Response<ResponseBody>,
  ) {
    route.handle {
      val response = try {
        action(KtorUserAgent(this), wasmoUrl(), call)
      } catch (e: UserException) {
        when (e) {
          is NotFoundUserException -> {
            // Don't log stack traces for these; everything is working as designed.
          }

          else -> logger.info("call failed", e)
        }
        call.respond(e.asResponse())
        return@handle
      }
      call.respond(response)
    }
  }

  private fun RoutingContext.wasmoUrl(): Url {
    val host = call.request.host()
    val dotIndex = host.length - rootUrl.topPrivateDomain.length - 1
    val (topPrivateDomain, subdomain) = when {
      dotIndex >= 1 && host.endsWith(rootUrl.topPrivateDomain) -> {
        rootUrl.topPrivateDomain to host.take(dotIndex)
      }

      else -> host to null
    }

    return rootUrl.copy(
      topPrivateDomain = topPrivateDomain,
      subdomain = subdomain,
      path = call.request.path().removePrefix("/").split("/"),
    )
  }

  private fun RoutingRequest.toRequest() = Request(
    method = call.request.httpMethod.value,
    url = call.url().toHttpUrl(),
    headers = buildList {
      for ((name, values) in headers.entries()) {
        for (value in values) {
          add(Header(name, value))
        }
      }
    },
    body = receiveChannel().asSource().asOkioSource().buffer().readByteString(),
  )

  @AssistedFactory
  interface Factory {
    fun create(route: Route): KtorActionBinder
  }
}
