/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqldelight.types

import com.squareup.javapoet.TypeName
import org.antlr.v4.runtime.ParserRuleContext

internal data class Value(
    internal val tableName: String?,
    internal val columnName: String?,
    internal val type: TypeName,
    internal val element: ParserRuleContext
)

internal fun List<Value>.columns(columnName: String, tableName: String?) = filter {
  it.columnName != null && it.columnName == columnName && (tableName == null || it.tableName == tableName)
}
