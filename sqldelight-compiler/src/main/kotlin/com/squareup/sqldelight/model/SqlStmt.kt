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
package com.squareup.sqldelight.model

import java.util.Locale.US

class SqlStmt<T>(identifier: String, stmt: String, startOffset: Int, allReplacements: List<SqlStmt.Replacement>,
    originatingElement: T) : SqlElement<T>(originatingElement) {

  val stmt: String
  val identifier = constantName(identifier)

  init {
    var nextOffset = 0
    this.stmt = allReplacements
        .filter({ it.startOffset > startOffset && it.endOffset < startOffset + stmt.length })
        .fold(StringBuilder(), { builder, replacement ->
          builder.append(stmt.substring(nextOffset, replacement.startOffset - startOffset)).append(
              replacement.replacementText)
          nextOffset = replacement.endOffset - startOffset
          builder
        })
        .append(stmt.substring(nextOffset, stmt.length))
        .toString()
  }

  data class Replacement(internal val startOffset: Int, internal val endOffset: Int, internal val replacementText: String)

  companion object {
    fun constantName(identifier: String) = identifier.toUpperCase(US)
  }
}
