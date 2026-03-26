package com.wasmo.identifiers

import dev.zacsweers.metro.Qualifier

@Qualifier
annotation class ForComputer

@Qualifier
annotation class ForHost

abstract class ComputerScope private constructor()
