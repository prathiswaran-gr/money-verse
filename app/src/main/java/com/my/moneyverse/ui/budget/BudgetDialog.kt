package com.my.moneyverse.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.util.minorToInput
import com.my.moneyverse.util.parseToMinor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDialog(
    initial: BudgetEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (categoryId: String?, limitMinor: Long, period: BudgetPeriod) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var categoryId by rememberSaveable { mutableStateOf(initial?.categoryId) }
    var amount by rememberSaveable { mutableStateOf(initial?.limitMinor?.let(::minorToInput).orEmpty()) }
    var period by rememberSaveable { mutableStateOf(initial?.period ?: BudgetPeriod.MONTHLY) }
    var menuOpen by remember { mutableStateOf(false) }
    val limitMinor = parseToMinor(amount)?.takeIf { it > 0 }
    val selectedName = categories.firstOrNull { it.id == categoryId }?.let { "${it.iconKey}  ${it.name}" }
        ?: "🎯  Overall (all expenses)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New budget" else "Edit budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("🎯  Overall (all expenses)") },
                            onClick = { categoryId = null; menuOpen = false },
                        )
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.iconKey}  ${c.name}") },
                                onClick = { categoryId = c.id; menuOpen = false },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d{0,9}(\\.\\d{0,2})?$"))) amount = input
                    },
                    label = { Text("Limit") },
                    prefix = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    BudgetPeriod.entries.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = period == p,
                            onClick = { period = p },
                            shape = SegmentedButtonDefaults.itemShape(i, BudgetPeriod.entries.size),
                        ) { Text(if (p == BudgetPeriod.WEEKLY) "Weekly" else "Monthly") }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = ExpenseRed) }
                }
                TextButton(onClick = { limitMinor?.let { onSave(categoryId, it, period) } }, enabled = limitMinor != null) {
                    Text("Save")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
