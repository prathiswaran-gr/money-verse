package com.my.moneyverse.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.my.moneyverse.data.BudgetProgress
import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.ui.appViewModel
import com.my.moneyverse.ui.components.BudgetBar
import com.my.moneyverse.ui.components.EmptyState
import com.my.moneyverse.ui.components.Eyebrow
import com.my.moneyverse.ui.components.GradientFab
import com.my.moneyverse.ui.components.MvCard
import com.my.moneyverse.ui.components.ScreenHeader
import com.my.moneyverse.ui.components.budgetColor
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.OnSurfaceDim
import com.my.moneyverse.util.formatMoney
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BudgetsScreen() {
    val vm = appViewModel { BudgetsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BudgetEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            GradientFab(text = "Budget", onClick = { editing = null; dialogOpen = true })
        },
    ) { padding ->
        val overall = state.items.firstOrNull { it.budget.categoryId == null }
        val others = state.items.filter { it !== overall }
        fun edit(p: BudgetProgress) { editing = p.budget; dialogOpen = true }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ScreenHeader(
                    eyebrow = "Budgets",
                    title = "${LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())} Budget",
                    horizontalPadding = 4.dp,
                )
            }
            if (!state.loading && state.items.isEmpty()) {
                item { EmptyState("🎯", "No budgets yet", "Set a weekly or monthly limit to keep spending in check.") }
            }
            if (overall != null) {
                item(key = overall.budget.id) { OverallCard(overall, Modifier.animateItem()) { edit(overall) } }
            }
            if (others.isNotEmpty()) {
                item(key = "categories") {
                    MvCard(Modifier.animateItem()) {
                        others.forEachIndexed { i, p ->
                            CategoryBudgetRow(p) { edit(p) }
                            if (i < others.lastIndex) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.04f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        val target = editing
        BudgetDialog(
            initial = target,
            categories = state.expenseCategories,
            onDismiss = { dialogOpen = false },
            onSave = { categoryId, limit, period ->
                vm.save(target, categoryId, limit, period)
                dialogOpen = false
            },
            onDelete = target?.let { b -> { vm.delete(b.id); dialogOpen = false } },
        )
    }
}

private val BudgetProgress.periodLabel: String
    get() = if (budget.period == BudgetPeriod.WEEKLY) "This week" else "This month"

@Composable
private fun OverallCard(progress: BudgetProgress, modifier: Modifier, onClick: () -> Unit) {
    val over = progress.remainingMinor < 0
    MvCard(modifier, onClick = onClick) {
        Column(Modifier.padding(20.dp)) {
            Eyebrow("Total spent • ${progress.periodLabel}", color = OnSurfaceDim)
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatMoney(progress.spentMinor, false), style = MaterialTheme.typography.headlineMedium)
                Text(
                    "of ${formatMoney(progress.budget.limitMinor, false)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDim,
                )
            }
            BudgetBar(progress.fraction)
            Text(
                if (over) "Over by ${formatMoney(-progress.remainingMinor, false)}"
                else "${formatMoney(progress.remainingMinor, false)} left • ${(progress.fraction * 100).toInt()}% used",
                style = MaterialTheme.typography.bodySmall,
                color = if (over) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(progress: BudgetProgress, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${progress.category?.iconKey ?: "🎯"}  ${progress.title}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${formatMoney(progress.spentMinor, false)} / ${formatMoney(progress.budget.limitMinor, false)}",
                style = MaterialTheme.typography.labelMedium,
                color = budgetColor(progress.fraction).takeIf { progress.fraction >= 0.8f } ?: OnSurfaceDim,
            )
        }
        Text(
            progress.periodLabel,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        BudgetBar(progress.fraction, height = 5.dp)
    }
}
