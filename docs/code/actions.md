Actions
=======

We use the word 'Action' to refer to HTTP actions (for example, to serve web page) and
JSON-over-HTTP RPCs.

We have our own mechanism to register actions with [our DI container](./dependency_injection.md).
There's a lot of indirection here! It is in service of keeping Wasmo OS modular: we compose each
distribution from a shared set of reusable modules.

Declaring Actions
-----------------

There’s a bunch of DI annotations on each action class.

 * `Inject`: grant Metro permission to call the class constructor.
 * `ClassKey`: this must be the same as the class itself.
 * `ContributesIntoMap`: make this class available to the `CallGraph` actions map.

Here's a sample for `HTTP`:

```kotlin
@Inject
@ClassKey(OsPage::class)
@ContributesIntoMap(CallScope::class)
class OsPage : HttpAction
```

And one for `RPC`. Note the `binding` parameter.

```kotlin
@Inject
@ClassKey(InstallAppRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class InstallAppRpc : RpcAction<InstallAppRequest, InstallAppResponse>
```

Declared actions are constructed by `CallGraph`, and can inject the `Client` to access who made this
request.


Registering Actions
-------------------

Each declared action must also be _registered_ which tells the web server which URLs activate the
action.

```kotlin
@Provides
@ElementsIntoSet
@SingleIn(OsScope::class)
fun provideActionRegistrations(
  hostnamePatterns: HostnamePatterns,
): List<ActionRegistration> = listOf(
  Http(
    host = hostnamePatterns.computerRegex,
    path = "/",
    method = "GET",
    action = OsPage::class,
  ),
)
```

Note that registrations are `OsScope` and the actions are `CallScope`.


Modular Actions
---------------

Each distribution declares its modules in a class like `HostedDistributionBindings`. An action
ships with a distribution if the action’s registration is included in the distribution’s bindings.

