package com.wasmo.support.wit.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.WitFile

class WitKotlinGenerator(
  private val witFile: WitFile,
) {
  fun generate(): FileSpec {
    val builder = FileSpec.builder("com.example", "Generated")

    for (declaration in witFile.declarations) {
      when (declaration) {
        is Interface -> {
          builder.addType(generate(declaration))
        }

        else -> Unit
      }
    }

    return builder.build()
  }

  private fun generate(
    declaration: Interface,
  ): TypeSpec {
    return TypeSpec.interfaceBuilder(declaration.name.name)
      .build()
  }
}
