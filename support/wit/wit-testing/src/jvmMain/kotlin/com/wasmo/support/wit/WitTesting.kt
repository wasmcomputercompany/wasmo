package com.wasmo.support.wit

import com.wasmo.support.wit.Include.Item

fun Case(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: TypeName? = null,
) = Case(
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
  cases: List<Case>,
) = Enum(
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
) = ExternalUsePath(
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
  flags: List<Flag>,
) = Flags(
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
  type: TypeName,
) = Field(
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
) = Flag(
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
  parameters: List<Parameter> = listOf(),
  returnType: TypeName? = null,
) = Function(
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
  items: List<Item> = listOf(),
) = Include(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  path = path.toUsePath(),
  items = items,
)

fun Interface(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  declarations: List<Declaration> = listOf(),
) = Interface(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  declarations = declarations,
)

fun Parameter(
  documentation: String? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  type: TypeName,
) = Parameter(
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
  fields: List<Field>,
) = Record(
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
  functions: List<Function> = listOf(),
) = Resource(
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
  target: TypeName,
) = TypeAlias(
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
  items: List<Use.Item>,
) = Use(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  path = path.toUsePath(),
  items = items,
)

fun Variant(
  documentation: String? = null,
  gate: Gate? = null,
  offset: Offset = Offset(1, 1),
  name: String,
  cases: List<Case>,
) = Variant(
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
  declarations: List<Declaration> = listOf(),
  imports: List<World.Api> = listOf(),
  exports: List<World.Api> = listOf(),
) = World(
  documentation = documentation?.let { Documentation(it) },
  gate = gate,
  offset = offset,
  name = Identifier(name),
  declarations = declarations,
  imports = imports,
  exports = exports,
)
