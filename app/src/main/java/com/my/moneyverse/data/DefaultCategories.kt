package com.my.moneyverse.data

import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TxnType

object DefaultCategories {
    private val expense = listOf(
        Triple("Food & Dining", "🍔", "#FF7043"),
        Triple("Groceries", "🛒", "#66BB6A"),
        Triple("Transport", "🚕", "#FFCA28"),
        Triple("Shopping", "🛍️", "#AB47BC"),
        Triple("Bills & Utilities", "💡", "#29B6F6"),
        Triple("Rent", "🏠", "#8D6E63"),
        Triple("Entertainment", "🎬", "#EC407A"),
        Triple("Health", "💊", "#26A69A"),
        Triple("Education", "📚", "#5C6BC0"),
        Triple("Travel", "✈️", "#42A5F5"),
        Triple("Other", "📦", "#9E9E9E"),
    )

    private val income = listOf(
        Triple("Salary", "💼", "#00E676"),
        Triple("Business", "📈", "#00C853"),
        Triple("Gifts", "🎁", "#FFD54F"),
        Triple("Other Income", "💰", "#B2FF59"),
    )

    val all: List<CategoryEntity> =
        expense.mapIndexed { i, (name, icon, color) ->
            CategoryEntity("sys_exp_$i", name, icon, color, TxnType.EXPENSE, i, isSystem = true)
        } + income.mapIndexed { i, (name, icon, color) ->
            CategoryEntity("sys_inc_$i", name, icon, color, TxnType.INCOME, 100 + i, isSystem = true)
        }
}
