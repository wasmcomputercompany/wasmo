package com.wasmo.identifiers

import dev.zacsweers.metro.Qualifier

@Qualifier
annotation class ForComputer

@Qualifier
annotation class ForOs

abstract class ComputerScope private constructor()

/** Singleton for the operating system. It is the parent to app and computer scopes. */
abstract class OsScope private constructor()
