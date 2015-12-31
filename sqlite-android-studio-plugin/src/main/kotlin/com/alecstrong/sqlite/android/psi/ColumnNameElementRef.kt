package com.alecstrong.sqlite.android.psi

import com.alecstrong.sqlite.android.SQLiteParser
import com.alecstrong.sqlite.android.lang.SqliteFile
import com.alecstrong.sqlite.android.lang.SqliteTokenTypes.RULE_ELEMENT_TYPES
import com.alecstrong.sqlite.android.psi.SqliteElement.ColumnNameElement
import com.alecstrong.sqlite.android.psi.SqliteElement.TableNameElement
import com.alecstrong.sqlite.android.util.childOfType
import com.alecstrong.sqlite.android.util.doRename
import com.alecstrong.sqlite.android.util.elementType
import com.alecstrong.sqlite.android.util.findUsages
import com.alecstrong.sqlite.android.util.parentOfType
import com.alecstrong.sqlite.android.util.prevSiblingOfType
import com.intellij.psi.PsiElement

internal class ColumnNameElementRef(idNode: IdentifierElement, ruleName: String)
: SqliteElementRef(idNode, ruleName) {
  private var leftTableDef: TableNameElement? = null

  override protected val identifierDefinitionRule = RULE_ELEMENT_TYPES[SQLiteParser.RULE_column_def]

  override fun getVariants(): Array<Any> {
    setLeftTable()
    return super.getVariants()
  }

  override fun resolve(): PsiElement? {
    val columnName = element.parentOfType<ColumnNameElement>()
    if (columnName != null && columnName.parent.elementType === identifierDefinitionRule) {
      // If this is already a column definition return ourselves.
      return columnName
    }
    setLeftTable()

    return super.resolve()
  }

  override fun handleElementRename(newElementName: String): PsiElement {
    val file = myElement.containingFile as SqliteFile

    val usageInfo = myElement.findUsages(newElementName)
    myElement.doRename(newElementName, usageInfo, file, null)

    return myElement
  }

  override fun isAccepted(element: PsiElement) =
      when (leftTableDef) {
        null -> super.isAccepted(element) || element is TableNameElement
            && element.getParent().elementType === RULE_ELEMENT_TYPES[SQLiteParser.RULE_create_table_stmt]
        else -> super.isAccepted(element)
            && leftTableDef!!.isSameTable(element.parent.parent.childOfType<TableNameElement>())
      }

  private fun setLeftTable() {
    leftTableDef = element.parentOfType<ColumnNameElement>()?.prevSiblingOfType<TableNameElement>()
  }
}
