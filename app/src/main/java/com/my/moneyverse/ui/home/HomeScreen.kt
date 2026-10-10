package com.my.moneyverse.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.ui.appViewModel
import com.my.moneyverse.ui.components.EmptyState
import com.my.moneyverse.ui.components.GradientFab
import com.my.moneyverse.ui.theme.Border
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.ui.theme.GreenTint
import com.my.moneyverse.ui.theme.OnSurfaceDim
import com.my.moneyverse.util.dayHeader
import com.my.moneyverse.util.formatMoney
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    val vm = appViewModel { HomeViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TransactionEntity?>(null) }

    fun onDeleted(id: String) {
        vm.delete(id)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar("Transaction deleted", actionLabel = "Undo")
            if (result == SnackbarResult.ActionPerformed) vm.undoDelete(id)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            GradientFab(
                text = "Add",
                onClick = { editing = null; sheetOpen = true },
                expanded = listState.firstVisibleItemIndex < 2,
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { HomeHeader() }
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    SummaryCard(state.monthSpentMinor, state.monthIncomeMinor, state.topBudget)
                }
            }
            item {
                Row(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TxnFilter.entries.forEach { f ->
                        PillChip(
                            label = f.name.lowercase().replaceFirstChar(Char::uppercase),
                            selected = state.filter == f,
                            onClick = { vm.setFilter(f) },
                        )
                    }
                }
            }
            if (!state.loading && state.days.isEmpty()) {
                item {
                    EmptyState("🪙", "No transactions yet", "Tap Add to record your first expense or income.")
                }
            }
            state.days.forEach { day ->
                stickyHeader(key = "h_${day.date}") {
                    DayHeader(day)
                }
                items(day.items, key = { it.id }) { txn ->
                    SwipeToDeleteRow(
                        txn = txn,
                        category = state.categories[txn.categoryId],
                        modifier = Modifier.animateItem(),
                        onClick = { editing = txn; sheetOpen = true },
                        onDelete = { onDeleted(txn.id) },
                    )
                }
            }
        }
    }

    if (sheetOpen) {
        val target = editing
        TransactionSheet(
            initial = target,
            categories = state.allCategories,
            onDismiss = { sheetOpen = false },
            onSave = { draft ->
                vm.save(target?.id, draft)
                sheetOpen = false
            },
            onDelete = target?.let { t -> { sheetOpen = false; onDeleted(t.id) } },
        )
    }
}

@Composable
private fun DayHeader(day: DayGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(dayHeader(day.date).uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
        Text(
            (if (day.netMinor >= 0) "+" else "") + formatMoney(day.netMinor),
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDim,
        )
    }
}

/** Pill toggle styled like the website's `.tag--green`. */
@Composable
private fun PillChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val bg by animateColorAsState(if (selected) GreenTint else Color.Transparent, label = "chipBg")
    val stroke by animateColorAsState(if (selected) Green.copy(alpha = 0.3f) else Border, label = "chipBorder")
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Green else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, stroke, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SwipeToDeleteRow(
    txn: TransactionEntity,
    category: CategoryEntity?,
    modifier: Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(ExpenseRed.copy(alpha = 0.85f))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        },
    ) {
        TransactionRow(txn, category, onClick)
    }
}
