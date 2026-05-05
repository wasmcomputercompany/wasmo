package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.WasmoJson
import com.wasmo.common.logging.Logger
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.framework.UserException
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
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
import io.ktor.server.routing.routing
import io.ktor.server.util.url
import io.ktor.utils.io.asSource
import kotlinx.io.okio.asOkioSource
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.buffer
import wasmo.http.Header

/**
 * Bridge our web framework ([ActionRegistration], [RpcAction], etc.) to Ktor's API.
 *
 * Our framework requires more set up than standard ktor, and in exchange for that we can contribute
 * actions from anywhere in the codebase with DI.
 */
@Inject
@SingleIn(OsScope::class)
class ActionRouter(
  private val application: Application,
  private val actionRegistrations: Set<ActionRegistration>,
  private val deployment: Deployment,
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
  private val logger: Logger,
) {
  private val rootUrl: Url
    get() = deployment.baseUrl.toString().decodeUrl()

  fun createRoutes() {
    application.install(CallLogging)
    application.routing {
      // Register actions from most precise to least precise.
      //  - RPCs (these don't collide with anything else)
      //  - HTTPs with a path and host
      //  - HTTPs with a path
      //  - other HTTPs
      //  - Static Resources
      val sortedActionRegistrations = actionRegistrations.toList().sortedBy {
        when (it) {
          is ActionRegistration.Rpc<*, *> -> 0
          is ActionRegistration.Http -> {
            when {
              it.pattern.host != null && it.pattern.path != null -> 1
              it.pattern.host != null -> 2
              else -> 3
            }
          }

          is ActionRegistration.StaticResources -> 4
        }
      }

      for (actionRegistration in sortedActionRegistrations) {
        register(actionRegistration)
      }
    }
  }

  private fun Route.register(actionRegistration: ActionRegistration) {
    when (actionRegistration) {
      is ActionRegistration.Http -> registerHttp(actionRegistration)
      is ActionRegistration.Rpc<*, *> -> registerRpc(actionRegistration)
      is ActionRegistration.StaticResources -> registerStaticResources(actionRegistration)
    }
  }

  private fun <S, R> Route.registerRpc(actionRegistration: ActionRegistration.Rpc<S, R>) {
    handleInternal(actionRegistration.pattern) { userAgent, wasmoUrl, call ->
      val request = actionRegistration.requestAdapter.decode(call.request)
      val action = callGraph(userAgent).rpcActions[actionRegistration.action]?.invoke()
        ?: error("unknown action: ${actionRegistration.action}, did you forget @ContributesIntoMap?")
      val response = (action as RpcAction<S, R>).invoke(userAgent, request, wasmoUrl)
      Response(
        status = response.status,
        headers = response.headers,
        contentType = response.contentType,
        body = ResponseBody { sink ->
          sink.writeUtf8(
            WasmoJson.encodeToString(
              actionRegistration.responseAdapter,
              response.body,
            ),
          )
        },
      )
    }
  }

  private fun Route.registerHttp(actionRegistration: ActionRegistration.Http) {
    handleInternal(actionRegistration.pattern) { userAgent, url, routingCall ->
      val action = callGraph(userAgent).httpActions[actionRegistration.action]?.invoke()
        ?: error("unknown action: ${actionRegistration.action}, did you forget @ContributesIntoMap?")
      action.invoke(userAgent, url, routingCall.request.toRequest())
    }
  }

  private fun Route.registerStaticResources(
    actionRegistration: ActionRegistration.StaticResources,
  ) {
    host(actionRegistration.host) {
      staticResources(actionRegistration.pathPrefix, actionRegistration.basePackage)
    }
  }

  private fun callGraph(userAgent: UserAgent): CallGraph {
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    clientAuthenticator.updateSessionCookie()
    return callGraphFactory.create(clientAuthenticator.get())
  }

  private fun Route.handleInternal(
    httpRequestPattern: HttpRequestPattern,
    action: suspend (UserAgent, Url, RoutingCall) -> Response<ResponseBody>,
  ) {
    childRoute(httpRequestPattern).handle {
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

  private fun Route.childRoute(pattern: HttpRequestPattern): Route {
    var result = this
    val host = pattern.host
    if (host != null) {
      result = result.createChild(
        HostRouteSelector(
          hostList = listOf(),
          hostPatterns = listOf(host),
          portsList = listOf(),
        ),
      )
    }

    val path = pattern.path
    result = when {
      path != null -> result.createRouteFromPath(path)
      else -> result.createChild(PathSegmentRegexRouteSelector(Regex(("/.*"))))
    }

    val method = pattern.method
    if (method != null) {
      result = result.createChild(HttpMethodRouteSelector(HttpMethod.parse(method)))
    }

    return result
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
}
