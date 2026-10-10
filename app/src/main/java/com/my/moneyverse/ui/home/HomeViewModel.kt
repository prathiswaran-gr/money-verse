package com.my.moneyverse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.moneyverse.data.BudgetProgress
import com.my.moneyverse.data.MoneyRepository
import com.my.moneyverse.data.computeBudgetProgress
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.util.periodBounds
import com.my.moneyverse.util.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TxnFilter { ALL, EXPENSE, INCOME }

data class DayGroup(val date: LocalDate, val items: List<TransactionEntity>, val netMinor: Long)

data class HomeUiState(
    val loading: Boolean = true,
    val monthSpentMinor: Long = 0,
    val monthIncomeMinor: Long = 0,
    val topBudget: BudgetProgress? = null,
    val filter: TxnFilter = TxnFilter.ALL,
    val days: List<DayGroup> = emptyList(),
    val categories: Map<String, CategoryEntity> = emptyMap(),
    val allCategories: List<CategoryEntity> = emptyList(),
)

class HomeViewModel(private val repo: MoneyRepository) : ViewModel() {
    private val filter = MutableStateFlow(TxnFilter.ALL)

    val state: StateFlow<HomeUiState> = combine(
        repo.transactions, repo.categories, repo.budgets, filter,
    ) { txns, categories, budgets, filter ->
        val month = periodBounds(BudgetPeriod.MONTHLY)
        val monthTxns = txns.filter { it.occurredAt in month }
        val visible = when (filter) {
            TxnFilter.ALL -> txns
            TxnFilter.EXPENSE -> txns.filter { it.type == TxnType.EXPENSE }
            TxnFilter.INCOME -> txns.filter { it.type == TxnType.INCOME }
        }
        HomeUiState(
            loading = false,
            monthSpentMinor = monthTxns.filter { it.type == TxnType.EXPENSE }.sumOf { it.amountMinor },
            monthIncomeMinor = monthTxns.filter { it.type == TxnType.INCOME }.sumOf { it.amountMinor },
            topBudget = computeBudgetProgress(budgets, txns, categories).maxByOrNull { it.fraction },
            filter = filter,
            days = visible.groupBy { it.occurredAt.toLocalDate() }.map { (date, items) ->
                DayGroup(date, items, items.sumOf { if (it.type == TxnType.INCOME) it.amountMinor else -it.amountMinor })
            },
            categories = categories.associateBy { it.id },
            allCategories = categories,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setFilter(value: TxnFilter) {
        filter.value = value
    }

    fun save(id: String?, draft: TxnDraft) = viewModelScope.launch {
        with(draft) {
            repo.saveTransaction(id, amountMinor, type, categoryId, paymentMode, occurredAt, merchant, note)
        }
    }

    fun delete(id: String) = viewModelScope.launch { repo.deleteTransaction(id) }

    fun undoDelete(id: String) = viewModelScope.launch { repo.restoreTransaction(id) }
}
