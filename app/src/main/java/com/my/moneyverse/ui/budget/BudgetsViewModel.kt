package com.my.moneyverse.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.moneyverse.data.BudgetProgress
import com.my.moneyverse.data.MoneyRepository
import com.my.moneyverse.data.computeBudgetProgress
import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TxnType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetsUiState(
    val loading: Boolean = true,
    val items: List<BudgetProgress> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
)

class BudgetsViewModel(private val repo: MoneyRepository) : ViewModel() {
    val state: StateFlow<BudgetsUiState> = combine(
        repo.budgets, repo.transactions, repo.categories,
    ) { budgets, txns, categories ->
        BudgetsUiState(
            loading = false,
            items = computeBudgetProgress(budgets, txns, categories),
            expenseCategories = categories.filter { it.type == TxnType.EXPENSE && !it.archived },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    fun save(existing: BudgetEntity?, categoryId: String?, limitMinor: Long, period: BudgetPeriod) =
        viewModelScope.launch { repo.saveBudget(existing, categoryId, limitMinor, period) }

    fun delete(id: String) = viewModelScope.launch { repo.deleteBudget(id) }
}
