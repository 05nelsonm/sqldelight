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
package com.squareup.sqldelight.gradle

import com.squareup.sqldelight.SqliteCompiler
import com.squareup.sqldelight.SqliteCompiler.Companion
import com.squareup.sqldelight.SqliteLexer
import com.squareup.sqldelight.SqliteParser
import com.squareup.sqldelight.SqliteParser.Create_table_stmtContext
import com.squareup.sqldelight.SqliteParser.Sql_stmtContext
import com.squareup.sqldelight.SqlitePluginException
import com.squareup.sqldelight.Status
import com.squareup.sqldelight.model.relativePath
import com.squareup.sqldelight.model.textWithWhitespace
import com.squareup.sqldelight.types.SymbolTable
import com.squareup.sqldelight.validation.SqlDelightValidator
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.apache.commons.lang3.StringUtils
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.StringTokenizer

open class SqlDelightTask : SourceTask() {
  private val sqliteCompiler = SqliteCompiler()
  private val sqldelightValidator = SqlDelightValidator()

  @get:OutputDirectory var outputDirectory: File? = null
  var buildDirectory: File? = null
    set(value) {
      field = value
      outputDirectory = File(buildDirectory, Companion.OUTPUT_DIRECTORY)
    }

  @TaskAction
  fun execute(inputs: IncrementalTaskInputs) {
    var symbolTable = SymbolTable()
    getInputs().files.forEach { file ->
      file.parseThen { parsed ->
        try {
          symbolTable = symbolTable.merge(SymbolTable(parsed), file.name)
        } catch (e: SqlitePluginException) {
          throw SqlitePluginException(e.originatingElement,
              Status.Failure(e.originatingElement, e.message).message(file))
        }
      }
    }

    inputs.outOfDate { inputFileDetails ->
      inputFileDetails.file.parseThen { parsed ->
        try {
          sqldelightValidator.validate(parsed, symbolTable)
        } catch (e: SqlitePluginException) {
          throw SqlitePluginException(e.originatingElement,
              Status.Failure(e.originatingElement, e.message).message(inputFileDetails.file))
        }
        val status = sqliteCompiler.write(
            parsed,
            inputFileDetails.file.nameWithoutExtension,
            inputFileDetails.file.absolutePath.relativePath(parsed),
            buildDirectory!!.parent + File.separatorChar,
            symbolTable
        )
        if (status is Status.Failure) {
          throw SqlitePluginException(status.originatingElement,
              status.message(inputFileDetails.file))
        }
      }
    }
  }

  private fun File.parseThen(operation: (SqliteParser.ParseContext) -> Unit) {
    if (!isDirectory) {
      try {
        val errorListener = ErrorListener(this)
        FileInputStream(this).use { inputStream ->
          val lexer = SqliteLexer(ANTLRInputStream(inputStream))
          lexer.removeErrorListeners()
          lexer.addErrorListener(errorListener)

          val parser = SqliteParser(CommonTokenStream(lexer))
          parser.removeErrorListeners()
          parser.addErrorListener(errorListener)

          val parsed = parser.parse()

          operation(parsed)
        }
      } catch (e: IOException) {
        throw IllegalStateException(e)
      }
    }
  }

  private fun Status.Failure.message(file: File) = "" +
      "${file.name} " +
      "line ${originatingElement.start.line}:${originatingElement.start.charPositionInLine}" +
      " - $errorMessage\n${detailText(originatingElement)}"

  private fun detailText(element: ParserRuleContext): String {
    val context = context(element) ?: element
    val result = StringBuilder()
    val tokenizer = StringTokenizer(context.textWithWhitespace(), "\n", false)

    val maxDigits = (Math.log10(context.stop.line.toDouble()) + 1).toInt()
    for (line in context.start.line..context.stop.line) {
      result.append(("%0${maxDigits}d    %s\n").format(line, tokenizer.nextToken()))
      if (element.start.line == element.stop.line && element.start.line == line) {
        // If its an error on a single line highlight where on the line.
        result.append(("%${maxDigits}s    ").format(""))
        if (element.start.charPositionInLine > 0) {
          result.append(("%${element.start.charPositionInLine}s").format(""))
        }
        result.append(("%s\n").format(
          StringUtils.repeat('^', element.stop.stopIndex - element.start.startIndex + 1)))
      }
    }

    return result.toString()
  }

  private fun context(element: ParserRuleContext?): ParserRuleContext? =
      when (element) {
        null -> element
        is Create_table_stmtContext -> element
        is Sql_stmtContext -> element
        else -> context(element.getParent())
      }
}
