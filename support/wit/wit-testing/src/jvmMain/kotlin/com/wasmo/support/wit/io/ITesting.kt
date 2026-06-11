package com.wasmo.support.wit.io

import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Gate
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset
import com.wasmo.support.wit.ir.IrCase
import com.wasmo.support.wit.ir.IrEnum
import com.wasmo.support.wit.ir.IrExternalUsePath
import com.wasmo.support.wit.ir.IrField
import com.wasmo.support.wit.ir.IrFlag
import com.wasmo.support.wit.ir.IrFlags
import com.wasmo.support.wit.ir.IrFunction
import com.wasmo.support.wit.ir.IrInterface
import com.wasmo.support.wit.ir.IrInterfaceName
import com.wasmo.support.wit.ir.IrParameter
import com.wasmo.support.wit.ir.IrRecord
import com.wasmo.support.wit.ir.IrResource
import com.wasmo.support.wit.ir.IrTypeAlias
import com.wasmo.support.wit.ir.IrTypeName
import com.wasmo.support.wit.ir.IrVariant
import com.wasmo.support.wit.ir.IrWorld

fun IrCase(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IrTypeName? = null,
) = IrCase(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IrTypeNameDeclared(
  packageName: String,
  parentName: String,
  typeName: String,
) = IrTypeName.Declared(
  packageName = packageName.toPackageName(),
  interfaceName = Identifier(parentName),
  name = Identifier(typeName),
)

fun IrEnum(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  cases: List<IrCase>,
) = IrEnum(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  cases = cases,
)

fun IrExternalUsePath(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  plainName: String? = null,
  path: IrInterfaceName,
) = IrExternalUsePath(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  plainName = plainName?.let { Identifier(it) },
  path = path,
)

fun IrFlags(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  flags: List<IrFlag>,
) = IrFlags(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  flags = flags,
)

fun IrField(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IrTypeName,
) = IrField(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IrFlag(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
) = IoFlag(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
)

fun IrFunction(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  async: Boolean = false,
  static: Boolean = false,
  constructor: Boolean = false,
  name: String,
  parameters: List<IrParameter> = listOf(),
  returnType: IrTypeName? = null,
) = IrFunction(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  async = async,
  static = static,
  constructor = constructor,
  name = Identifier(name),
  parameters = parameters,
  returnType = returnType,
)

fun IrInterface(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  items: List<IrInterface.Item> = listOf(),
) = IrInterface(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  items = items,
)

fun IrInterfaceName(
  packageName: String,
  name: String,
) = IrInterfaceName(
  packageName = packageName.toPackageName(),
  name = Identifier(name),
)

fun IrParameter(
  documentation: String? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IrTypeName,
) = IrParameter(
  documentation = documentation?.let { Documentation(it) },
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IrRecord(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  fields: List<IrField>,
) = IrRecord(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  fields = fields,
)

fun IrResource(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  functions: List<IrFunction> = listOf(),
) = IrResource(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  functions = functions,
)

fun IrTypeAlias(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  target: IrTypeName,
) = IrTypeAlias(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  target = target,
)

fun IrVariant(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  cases: List<IrCase>,
) = IrVariant(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  cases = cases,
)

fun IrWorld(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  items: List<IrWorld.Item> = listOf(),
  imports: List<IrWorld.Api> = listOf(),
  exports: List<IrWorld.Api> = listOf(),
) = IrWorld(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  items = items,
  imports = imports,
  exports = exports,
)
