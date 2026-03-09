Dependency Injection
====================

We're using [Metro] for dependency injection. We use it in both production code and test code.

Test code does not create production dependency graphs; it's one or the other in each process.


Production
----------

```kotlin
/**
 * The service root graph.
 */
@AppScope
fun service(
  config: WasmoService.Config,
  embeddedServer: EmbeddedServer<*, *>,
  wasmoDb: WasmoDb,
) : WasmoServiceGraph

/**
 * For the duration of each API call, there's a short-lived call graph.
 */
@CallScope
fun WasmoServiceGraph.call(
  client: Client,
): CallGraph

/**
 * There's also a graph for each Computer.
 */
@ComputerScope
fun WasmoServiceGraph.computer(
  computerId: ComputerId,
  slug: ComputerSlug,
) : ComputerGraph
```

Testing
-------

```kotlin
/**
 * The root for each test.
 */
@AppScope
fun serviceTester(
  wasmoDbService: WasmoDbService,
  coroutineScope: CoroutineScope,
): ServiceTesterGraph

/**
 * Tests have a graph for each user. There's no equivalent to this graph for production!
 */
@ClientScope
fun ServiceTesterGraph.client(
  clientAuthenticator: ClientAuthenticator,
  sessionCookie: SessionCookie,
): ClientTesterGraph

/**
 * Tests also have a graph for each API call.
 */
@CallScope
fun ClientTesterGraph.call(
  client: Client,
): CallTesterGraph

/**
 * The computer graph extends the client graph in test. This is different from production!
 */
@ComputerScope
fun ClientTesterGraph.computer(
  slug: ComputerSlug,
): ComputerTesterGraph

/**
 * The installed app graph extends the computer graph.
 */
@InstalledAppScope
fun ComputerTesterGraph.installedApp(
  publishedApp: PublishedApp,
): InstalledAppTesterGraph
```

[Metro]: https://github.com/ZacSweers/metro
