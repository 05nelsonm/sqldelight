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
package com.squareup.sqldelight.util

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.tree.IElementType
import com.squareup.sqldelight.SqliteParser
import com.squareup.sqldelight.lang.SqliteLanguage
import org.antlr.intellij.adaptor.lexer.ElementTypeFactory

object SqlitePsiUtils {
  fun createLeafFromText(project: Project, context: PsiElement?, text: String, type: IElementType) =
      (PsiFileFactory.getInstance(project) as PsiFileFactoryImpl)
          .createElementFromText(text, SqliteLanguage.INSTANCE, type, context)!!.getDeepestFirst()
}

internal val RULES = SqliteParser.ruleNames.asList()
private val TOKENS = SqliteParser.tokenNames.asList()

internal fun ASTNode.childrenWithRules(vararg rules: Int) = getChildren(
    ElementTypeFactory.createRuleSet(SqliteLanguage.INSTANCE, RULES, *rules))

internal fun ASTNode.childrenWithTokens(vararg tokens: Int) = getChildren(
    ElementTypeFactory.createTokenSet(SqliteLanguage.INSTANCE, TOKENS, *tokens))
