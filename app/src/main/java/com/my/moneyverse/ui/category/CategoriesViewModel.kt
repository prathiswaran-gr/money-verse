package com.my.moneyverse.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.moneyverse.data.MoneyRepository
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TxnType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repo: MoneyRepository) : ViewModel() {
    val categories: StateFlow<List<CategoryEntity>> =
        repo.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(existing: CategoryEntity?, name: String, emoji: String, colorHex: String, type: TxnType) =
        viewModelScope.launch { repo.saveCategory(existing, name, emoji, colorHex, type) }

    fun setArchived(id: String, archived: Boolean) =
        viewModelScope.launch { repo.setCategoryArchived(id, archived) }
}
