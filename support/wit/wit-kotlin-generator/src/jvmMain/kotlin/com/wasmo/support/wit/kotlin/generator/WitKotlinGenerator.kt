package com.wasmo.support.wit.kotlin.generator

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
      "Wit",
    )
    for (witFile in witPackage.files.values) {
      addDeclarations(
        builder = builder,
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

  private fun setDeclaration(
    builder: Documentable.Builder<*>,
    declaration: Declaration? = null,
  ) {
    val documentation = declaration?.documentation?.content
    if (documentation != null) {
      builder.addKdoc(documentation.trimIndent())
    }
  }

  private fun addDeclarations(
    builder: Any,
    typeResolver: PackageTypeMapper,
    children: List<Declaration>,
  ) {
    for (declaration in children) {
      val spec = generate(typeResolver, declaration) ?: continue
      add(builder, spec)
    }
  }

  private fun generate(
    scope: PackageTypeMapper,
    declaration: Declaration,
  ): Any? {
    return when (declaration) {
      is Case -> error("unexpected call")
      is Enum -> null
      is Export -> null
      is Field -> error("unexpected call")
      is Flag -> null
      is Flags -> null
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
      is Variant -> generateVariant(scope, declaration)
      is World -> null
    }
  }

  private fun generateInterface(
    scope: PackageTypeMapper,
    `interface`: Interface,
  ): TypeSpec {
    val interfaceScope = scope.refine(interfaceName = `interface`.name)
    val builder = TypeSpec.interfaceBuilder(interfaceScope.className.simpleName)
    setDeclaration(builder, `interface`)
    addDeclarations(
      builder = builder,
      typeResolver = interfaceScope,
      children = `interface`.declarations,
    )
    return builder.build()
  }

  private fun generateRecord(
    scope: PackageTypeMapper,
    record: Record,
  ): TypeSpec {
    val classBuilder = TypeSpec.classBuilder(record.name.name.toCamelCase(upperCamel = true))
      .addModifiers(KModifier.DATA)
    setDeclaration(
      builder = classBuilder,
      declaration = record,
    )

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
          .apply {
            setDeclaration(this, field)
          }
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
    val classBuilder = TypeSpec.interfaceBuilder(record.name.name.toCamelCase(upperCamel = true))
    setDeclaration(
      builder = classBuilder,
      declaration = record,
    )
    addDeclarations(
      builder = classBuilder,
      typeResolver = scope,
      children = record.functions,
    )
    return classBuilder.build()
  }

  private fun generateVariant(
    scope: PackageTypeMapper,
    record: Variant,
  ): TypeSpec {
    val classBuilder = TypeSpec.interfaceBuilder(record.name.name.toCamelCase(upperCamel = true))
    setDeclaration(
      builder = classBuilder,
      declaration = record,
    )
    for (case in record.cases) {
      val caseBuilder = TypeSpec.objectBuilder(case.name.name.toCamelCase(upperCamel = true))
      setDeclaration(
        builder = caseBuilder,
        declaration = case,
      )
      classBuilder.addType(caseBuilder.build())
    }
    return classBuilder.build()
  }

  private fun generateFunction(
    scope: PackageTypeMapper,
    function: Function,
  ): FunSpec {
    val builder = FunSpec.builder(function.name.name)
      .addModifiers(KModifier.ABSTRACT)

    val returnType = function.returnType
    if (returnType != null) {
      builder.returns(scope.map(returnType))
    }

    setDeclaration(
      builder = builder,
      declaration = function,
    )

    return builder.build()
  }
}
