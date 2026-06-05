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
import com.wasmo.support.wit.Case
import com.wasmo.support.wit.Declaration
import com.wasmo.support.wit.Enum
import com.wasmo.support.wit.Export
import com.wasmo.support.wit.Field
import com.wasmo.support.wit.Flag
import com.wasmo.support.wit.Flags
import com.wasmo.support.wit.Function
import com.wasmo.support.wit.Import
import com.wasmo.support.wit.Include
import com.wasmo.support.wit.Interface
import com.wasmo.support.wit.Package
import com.wasmo.support.wit.Record
import com.wasmo.support.wit.Resource
import com.wasmo.support.wit.SymbolResolver
import com.wasmo.support.wit.TopLevelUse
import com.wasmo.support.wit.TypeAlias
import com.wasmo.support.wit.Use
import com.wasmo.support.wit.Variant
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.World

class WitKotlinGenerator(
  private val witPackages: List<WitPackage>,
  private val kotlinPackagePrefix: String = "wit",
) {
  private val typeMapper = TypeMapper(
    symbolResolver = SymbolResolver(witPackages),
    kotlinPackagePrefix = kotlinPackagePrefix,
  )

  fun generate(): List<FileSpec> = witPackages.map { generate(it) }

  private fun generate(witPackage: WitPackage): FileSpec {
    val builder = FileSpec.builder(
      witPackage.packageName?.toKotlin(kotlinPackagePrefix) ?: kotlinPackagePrefix,
      "Api",
    )
    for (witFile in witPackage.files.values) {
      builder.addDeclarations(
        typeResolver = typeMapper.refine(witPackage.packageName),
        children = witFile.declarations,
      )
    }
    return builder.build()
  }

  private fun add(
    builder: Any,
    spec: Any,
  ) {
    when (spec) {
      is PropertySpec if builder is MemberSpecHolder.Builder<*> -> builder.addProperty(spec)
      is FunSpec if builder is MemberSpecHolder.Builder<*> -> builder.addFunction(spec)
      is TypeSpec if builder is TypeSpecHolder.Builder<*> -> builder.addType(spec)
      else -> error("cannot add ${spec::class.simpleName} to ${builder::class.qualifiedName}")
    }
  }

  private fun <T : Documentable.Builder<*>> T.setDeclaration(
    declaration: Declaration? = null,
  ): T = apply {
    val documentation = declaration?.documentation?.content
    if (documentation != null) {
      addKdoc(documentation.trimIndent())
    }
  }

  private fun <T : Any> T.addDeclarations(
    typeResolver: PackageTypeMapper,
    children: List<Declaration>,
  ): T = apply {
    for (declaration in children) {
      val spec = generate(typeResolver, declaration) ?: continue
      add(this, spec)
    }
  }

  private fun generate(
    scope: PackageTypeMapper,
    declaration: Declaration,
  ): Any? {
    return when (declaration) {
      is Case -> error("unexpected call")
      is Enum -> generateEnum(scope as InterfaceTypeMapper, declaration)
      is Export -> null
      is Field -> error("unexpected call")
      is Flag -> error("unexpected call")
      is Flags -> generateFlags(scope as InterfaceTypeMapper, declaration)
      is Function -> generateFunction(scope, declaration)
      is Import -> null
      is Include -> null
      is Interface -> generateInterface(scope, declaration)
      is Package -> null
      is Record -> generateRecord(scope, declaration)
      is Resource -> generateResource(scope, declaration)
      is TopLevelUse -> null
      is TypeAlias -> null
      is Use -> null
      is Variant -> generateVariant(scope as InterfaceTypeMapper, declaration)
      is World -> null
    }
  }

  private fun generateInterface(
    scope: PackageTypeMapper,
    `interface`: Interface,
  ): TypeSpec {
    val interfaceScope = scope.refine(interfaceName = `interface`.name)
    return TypeSpec.interfaceBuilder(interfaceScope.className.simpleName)
      .setDeclaration(`interface`)
      .addDeclarations(
        typeResolver = interfaceScope,
        children = `interface`.declarations,
      )
      .build()
  }

  private fun generateRecord(
    scope: PackageTypeMapper,
    record: Record,
  ): TypeSpec {
    val classBuilder = TypeSpec.classBuilder(record.name.name.toCamelCase(upperCamel = true))
      .addModifiers(KModifier.DATA)
      .setDeclaration(record)

    val constructorBuilder = FunSpec.constructorBuilder()

    for (field in record.fields) {
      val type = scope.map(field.type)
      val name = field.name.name
      val parameter = ParameterSpec.builder(name, type)
        .build()
      constructorBuilder.addParameter(parameter)

      classBuilder.addProperty(
        PropertySpec.builder(name, type)
          .initializer("%N", parameter)
          .setDeclaration(field)
          .build(),
      )
    }

    classBuilder.primaryConstructor(constructorBuilder.build())

    return classBuilder.build()
  }

  private fun generateResource(
    scope: PackageTypeMapper,
    record: Resource,
  ): TypeSpec {
    return TypeSpec.interfaceBuilder(record.name.name.toCamelCase(upperCamel = true))
      .setDeclaration(record)
      .addDeclarations(
        typeResolver = scope,
        children = record.functions,
      )
      .build()
  }

  private fun generateVariant(
    scope: InterfaceTypeMapper,
    variant: Variant,
  ): TypeSpec {
    val variantName = variant.name.name.toCamelCase(upperCamel = true)
    val classBuilder = TypeSpec.interfaceBuilder(variantName)
      .addModifiers(KModifier.SEALED)
      .setDeclaration(variant)
    for (case in variant.cases) {
      val type = case.type
      val caseName = case.name.name.toCamelCase(upperCamel = true)
      if (type != null) {
        val kotlinType = scope.map(type)
        classBuilder.addType(
          TypeSpec.classBuilder(caseName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(scope.className.nestedClass(variantName))
            .primaryConstructor(
              FunSpec.constructorBuilder()
                .addParameter("value", kotlinType)
                .build(),
            )
            .addProperty(
              PropertySpec.builder("value", kotlinType)
                .initializer("%N", "value")
                .build(),
            )
            .setDeclaration(case)
            .build(),
        )
      } else {
        classBuilder.addType(
          TypeSpec.objectBuilder(caseName)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(scope.className.nestedClass(variantName))
            .setDeclaration(case)
            .build(),
        )
      }
    }
    return classBuilder.build()
  }

  private fun generateEnum(
    scope: InterfaceTypeMapper,
    enum: Enum,
  ): TypeSpec {
    val enumName = enum.name.name.toCamelCase(upperCamel = true)
    val classBuilder = TypeSpec.enumBuilder(enumName)
      .addModifiers(KModifier.SEALED)
      .setDeclaration(enum)
    for (case in enum.cases) {
      check(case.type == null)
      val caseName = case.name.name.toCamelCase(upperCamel = true)
      classBuilder.addEnumConstant(
        caseName,
        TypeSpec.anonymousClassBuilder()
          .setDeclaration(case)
          .build(),
      )
    }
    return classBuilder.build()
  }

  private fun generateFlags(
    scope: InterfaceTypeMapper,
    flags: Flags,
  ): TypeSpec {
    val classBuilder = TypeSpec.classBuilder(flags.name.name.toCamelCase(upperCamel = true))
      .addModifiers(KModifier.DATA)
      .setDeclaration(flags)

    val constructorBuilder = FunSpec.constructorBuilder()

    for (field in flags.flags) {
      val name = field.name.name.toCamelCase(upperCamel = false)
      val parameter = ParameterSpec.builder(name, BOOLEAN)
        .build()
      constructorBuilder.addParameter(parameter)

      classBuilder.addProperty(
        PropertySpec.builder(name, BOOLEAN)
          .initializer("%N", parameter)
          .setDeclaration(field)
          .build(),
      )
    }

    classBuilder.primaryConstructor(constructorBuilder.build())

    return classBuilder.build()
  }

  private fun generateFunction(
    scope: PackageTypeMapper,
    function: Function,
  ): FunSpec {
    return FunSpec.builder(function.name.name)
      .addModifiers(KModifier.ABSTRACT)
      .setDeclaration(function)
      .apply {
        val returnType = function.returnType
        if (returnType != null) {
          returns(scope.map(returnType))
        }
      }.build()
  }
}
