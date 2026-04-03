package com.wasmo.identifiers

import dev.zacsweers.metro.Qualifier

/** Singleton for the operating system. It is the parent to app and computer scopes. */
abstract class OsScope private constructor()

@Qualifier
annotation class ForOs

/** Child scope of [OsScope]. */
abstract class ComputerScope private constructor()

@Qualifier
annotation class ForComputer

/** Child scope of [OsScope]. */
abstract class InstalledAppScope private constructor()

@Qualifier
annotation class ForInstalledApp
