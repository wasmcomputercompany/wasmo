package com.wasmo.support.wit.io

import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Gate
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset

fun IoCase(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IoTypeName? = null,
) = IoCase(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IoEnum(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  cases: List<IoCase>,
) = IoEnum(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  cases = cases,
)

fun IoExternalApi(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  plainName: String? = null,
  path: String,
) = IoExternalApi(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  plainName = plainName?.let { Identifier(it) },
  path = path.toUsePath(),
)

fun IoFlags(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  flags: List<IoFlag>,
) = IoFlags(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  flags = flags,
)

fun IoField(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IoTypeName,
) = IoField(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IoFlag(
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

fun IoFunction(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  async: Boolean = false,
  static: Boolean = false,
  constructor: Boolean = false,
  name: String,
  parameters: List<IoParameter> = listOf(),
  returnType: IoTypeName? = null,
) = IoFunction(
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

fun IoInclude(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  path: String,
  items: List<IoInclude.Item> = listOf(),
) = IoInclude(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  path = path.toUsePath(),
  items = items,
)

fun IoIncludeItem(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  type: String,
  alias: String,
) = IoInclude.Item(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  type = IoTypeName.Declared(type),
  alias = Identifier(alias),
)

fun IoInterface(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  items: List<IoInterface.Item> = listOf(),
) = IoInterface(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  items = items,
)

fun IoParameter(
  documentation: String? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: IoTypeName,
) = IoParameter(
  documentation = documentation?.let { Documentation(it) },
  offset = offset,
  name = Identifier(name),
  type = type,
)

fun IoRecord(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  fields: List<IoField>,
) = IoRecord(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  fields = fields,
)

fun IoResource(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  functions: List<IoFunction> = listOf(),
) = IoResource(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  functions = functions,
)

fun IoTypeAlias(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  target: IoTypeName,
) = IoTypeAlias(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  target = target,
)

fun IoUse(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  path: String,
  items: List<IoUse.Item>,
) = IoUse(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  path = path.toUsePath(),
  items = items,
)

fun IoUseItem(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  type: String,
  alias: String? = null,
) = IoUse.Item(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  type = IoTypeName.Declared(type),
  alias = alias?.let { Identifier(it) },
)

fun IoVariant(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  cases: List<IoCase>,
) = IoVariant(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  cases = cases,
)

fun IoWorld(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  items: List<IoWorld.Item> = listOf(),
  imports: List<IoWorld.Api> = listOf(),
  exports: List<IoWorld.Api> = listOf(),
) = IoWorld(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  items = items,
  imports = imports,
  exports = exports,
)
