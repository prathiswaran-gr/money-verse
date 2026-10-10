package com.my.moneyverse.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.ui.appViewModel
import com.my.moneyverse.ui.components.CategoryIcon
import com.my.moneyverse.ui.components.GradientFab
import com.my.moneyverse.ui.components.ScreenHeader
import com.my.moneyverse.ui.theme.Border
import com.my.moneyverse.ui.theme.Green

@Composable
fun CategoriesScreen() {
    val vm = appViewModel { CategoriesViewModel(it) }
    val categories by vm.categories.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(TxnType.EXPENSE) }
    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            GradientFab(text = "Category", onClick = { editing = null; dialogOpen = true })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item {
                ScreenHeader(eyebrow = "Organise", title = "Categories")
                PrimaryTabRow(
                    selectedTabIndex = tab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = Green,
                    divider = { HorizontalDivider(color = Border) },
                ) {
                    TxnType.entries.forEach { t ->
                        Tab(
                            selected = tab == t,
                            onClick = { tab = t },
                            text = { Text(if (t == TxnType.EXPENSE) "Expense" else "Income", style = MaterialTheme.typography.titleSmall) },
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(categories.filter { it.type == tab }, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    modifier = Modifier.animateItem(),
                    onClick = { editing = category; dialogOpen = true },
                    onToggleArchive = { vm.setArchived(category.id, !category.archived) },
                )
            }
        }
    }

    if (dialogOpen) {
        CategoryDialog(
            initial = editing,
            defaultType = tab,
            onDismiss = { dialogOpen = false },
            onSave = { name, emoji, color, type ->
                vm.save(editing, name, emoji, color, type)
                dialogOpen = false
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    modifier: Modifier,
    onClick: () -> Unit,
    onToggleArchive: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (category.archived) 0.45f else 1f)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(category.iconKey, category.colorHex)
        Spacer(Modifier.width(14.dp))
        Text(
            category.name + if (category.archived) " (archived)" else "",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onToggleArchive) {
            Icon(
                if (category.archived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                contentDescription = if (category.archived) "Unarchive" else "Archive",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
