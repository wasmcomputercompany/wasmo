package com.wasmo.support.wit.kotlin.generator

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.Documentable
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberSpecHolder
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpecHolder

class ApiGenerator {
  fun generate(witPackage: WitPackageKt): FileSpec {
    return FileSpec.builder(witPackage.packageName, "Api")
      .apply {
        for (declaration in witPackage.declarations) {
          add(this, declaration)
        }
      }
      .build()
  }

  private fun add(builder: Any, value: DeclarationKt) {
    when (value) {
      is EnumKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is FlagsKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is FunctionKt -> (builder as MemberSpecHolder.Builder<*>).addFunction(generate(value))
      is InterfaceKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is RecordKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is ResourceKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is VariantKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      is WorldKt -> (builder as TypeSpecHolder.Builder<*>).addType(generate(value))
      else -> error("unexpected value: $value")
    }
  }

  private fun <T : Documentable.Builder<*>> T.setDeclaration(
    declaration: DeclarationKt? = null,
  ): T = apply {
    val documentation = declaration?.documentation
    if (documentation != null) {
      addKdoc(documentation.trimIndent())
    }
  }

  private fun generate(value: InterfaceKt) = TypeSpec.interfaceBuilder(value.type.simpleName)
    .setDeclaration(value)
    .apply {
      for (declaration in value.declarations) {
        add(this, declaration)
      }
    }
    .build()

  private fun generate(value: RecordKt): TypeSpec {
    val classBuilder = TypeSpec.classBuilder(value.type.simpleName)
      .addModifiers(KModifier.DATA)
      .setDeclaration(value)

    val constructorBuilder = FunSpec.constructorBuilder()

    for (field in value.fields) {
      val name = field.name
      val parameter = ParameterSpec.builder(name, field.type)
        .build()
      constructorBuilder.addParameter(parameter)

      classBuilder.addProperty(
        PropertySpec.builder(name, field.type)
          .initializer("%N", parameter)
          .setDeclaration(field)
          .build(),
      )
    }

    classBuilder.primaryConstructor(constructorBuilder.build())

    return classBuilder.build()
  }

  private fun generate(value: ResourceKt) = TypeSpec.interfaceBuilder(value.type.simpleName)
    .setDeclaration(value)
    .apply {
      for (function in value.functions) {
        addFunction(generate(function))
      }
    }
    .build()

  private fun generate(value: VariantKt) = TypeSpec.interfaceBuilder(value.type.simpleName)
    .addModifiers(KModifier.SEALED)
    .setDeclaration(value)
    .apply {
      for (case in value.cases) {
        val type = case.type
        if (type != null) {
          addType(
            TypeSpec.classBuilder(case.name)
              .addModifiers(KModifier.DATA)
              .addSuperinterface(value.type)
              .primaryConstructor(
                FunSpec.constructorBuilder()
                  .addParameter("value", case.type)
                  .build(),
              )
              .addProperty(
                PropertySpec.builder("value", case.type)
                  .initializer("%N", "value")
                  .build(),
              )
              .setDeclaration(case)
              .build(),
          )
        } else {
          addType(
            TypeSpec.objectBuilder(case.name)
              .addModifiers(KModifier.DATA)
              .addSuperinterface(value.type)
              .setDeclaration(case)
              .build(),
          )
        }
      }
    }
    .build()

  private fun generate(value: EnumKt) = TypeSpec.enumBuilder(value.type.simpleName)
    .addModifiers(KModifier.SEALED)
    .setDeclaration(value)
    .apply {
      for (case in value.cases) {
        addEnumConstant(
          case.name,
          TypeSpec.anonymousClassBuilder()
            .setDeclaration(case)
            .build(),
        )
      }
    }
    .build()

  private fun generate(value: FlagsKt) = TypeSpec.classBuilder(value.type.simpleName)
    .addModifiers(KModifier.DATA)
    .setDeclaration(value)
    .apply {
      val constructorBuilder = FunSpec.constructorBuilder()

      for (field in value.flags) {
        val parameter = ParameterSpec.builder(field.name, BOOLEAN)
          .build()
        constructorBuilder.addParameter(parameter)
        addProperty(
          PropertySpec.builder(field.name, BOOLEAN)
            .initializer("%N", parameter)
            .setDeclaration(field)
            .build(),
        )
      }

      primaryConstructor(constructorBuilder.build())
    }
    .build()

  private fun generate(value: FunctionKt) = FunSpec.builder(value.name)
    .addModifiers(KModifier.ABSTRACT)
    .setDeclaration(value)
    .apply {
      for (parameter in value.parameters) {
        addParameter(parameter.name, parameter.type)
      }
      val returnType = value.returnType
      if (returnType != null) {
        returns(returnType)
      }
    }
    .build()

  private fun generate(value: WorldKt) = TypeSpec.objectBuilder(value.type.simpleName)
    .setDeclaration(value)
    .apply {
      if (value.exports.isNotEmpty()) {
        addType(
          TypeSpec.interfaceBuilder("Guest")
            .apply {
              for (export in value.exports) {
                addProperty(export.name, export.type)
              }
            }
            .build(),
        )
      }
      if (value.imports.isNotEmpty()) {
        addType(
          TypeSpec.interfaceBuilder("Host")
            .apply {
              for (import in value.imports) {
                addProperty(import.name, import.type)
              }
            }
            .build(),
        )
      }
    }
    .build()
}
