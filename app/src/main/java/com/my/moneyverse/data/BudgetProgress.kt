package com.my.moneyverse.data

import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.util.periodBounds
import java.time.LocalDate

data class BudgetProgress(
    val budget: BudgetEntity,
    val category: CategoryEntity?,
    val spentMinor: Long,
) {
    val fraction: Float get() = if (budget.limitMinor <= 0) 0f else spentMinor.toFloat() / budget.limitMinor
    val remainingMinor: Long get() = budget.limitMinor - spentMinor
    val title: String get() = category?.name ?: "Overall"
}

fun computeBudgetProgress(
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    today: LocalDate = LocalDate.now(),
): List<BudgetProgress> {
    val byId = categories.associateBy { it.id }
    val expenses = transactions.filter { it.type == TxnType.EXPENSE }
    return budgets.map { budget ->
        val bounds = periodBounds(budget.period, today)
        val spent = expenses
            .filter { it.occurredAt in bounds }
            .filter { budget.categoryId == null || it.categoryId == budget.categoryId }
            .sumOf { it.amountMinor }
        BudgetProgress(budget, budget.categoryId?.let(byId::get), spent)
    }
}
