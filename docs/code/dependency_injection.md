Dependency Injection
====================

We're using [Metro] for dependency injection. We use it in both production code and test code.

Test code does not create production dependency graphs; it's one or the other in each process.

The code below is pseudocode. I'm attempting to show the shape of the DI graphs without the full
syntax of graph declarations and factories.


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
 * There's also a graph for each computer.
 */
@ComputerScope
fun WasmoServiceGraph.computer(
  computerId: ComputerId,
  slug: ComputerSlug,
) : ComputerServiceGraph

/**
 * There's also a graph for each installed app.
 */
@InstalledAppScope
fun WasmoServiceGraph.installedApp(
  computerSlug: ComputerSlug,
  installedApp: InstalledApp,
) : InstalledAppServiceGraph
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
 * Tests also have a graph for each API call.
 */
@CallScope
fun ClientTesterGraph.call(
  client: Client,
): CallTesterGraph
```

[Metro]: https://github.com/ZacSweers/metro
