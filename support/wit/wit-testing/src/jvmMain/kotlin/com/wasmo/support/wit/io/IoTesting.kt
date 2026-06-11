package com.wasmo.support.wit.io

import com.wasmo.support.wit.Documentation
import com.wasmo.support.wit.Gate
import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.Offset

fun Case(
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

fun Enum(
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

fun ExternalUsePath(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  plainName: String? = null,
  path: String,
) = IoExternalUsePath(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  plainName = plainName?.let { Identifier(it) },
  path = path.toUsePath(),
)

fun Flags(
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

fun Field(
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

fun Flag(
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

fun Function(
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

fun Gate(
  unstable: String? = null,
  since: String? = null,
  deprecated: String? = null,
) = Gate(
  unstable = unstable?.let { Identifier(it) },
  since = since?.toSemVer(),
  deprecated = deprecated?.toSemVer(),
)

fun Include(
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

fun IncludeItem(
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

fun Interface(
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

fun Scope(
  packageName: String,
  interfaceName: String? = null,
) = Scope(
  packageName = packageName.toPackageName(),
  interfaceName = interfaceName?.let { Identifier(it) },
)

fun Parameter(
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

fun Record(
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

fun Resource(
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

fun TypeAlias(
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

fun Use(
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

fun UseItem(
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

fun Variant(
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

fun World(
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
