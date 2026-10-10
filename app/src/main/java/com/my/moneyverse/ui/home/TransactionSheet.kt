package com.my.moneyverse.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.PaymentMode
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.ui.components.CategoryIcon
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.util.formatFullDate
import com.my.moneyverse.util.minorToInput
import com.my.moneyverse.util.parseToMinor
import com.my.moneyverse.util.toLocalDate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

data class TxnDraft(
    val amountMinor: Long,
    val type: TxnType,
    val categoryId: String,
    val paymentMode: PaymentMode,
    val occurredAt: Long,
    val merchant: String,
    val note: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    initial: TransactionEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TxnDraft) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by rememberSaveable { mutableStateOf(initial?.amountMinor?.let(::minorToInput).orEmpty()) }
    var type by rememberSaveable { mutableStateOf(initial?.type ?: TxnType.EXPENSE) }
    var categoryId by rememberSaveable { mutableStateOf(initial?.categoryId) }
    var mode by rememberSaveable { mutableStateOf(initial?.paymentMode ?: PaymentMode.UPI) }
    var date by rememberSaveable { mutableStateOf(initial?.occurredAt?.toLocalDate() ?: LocalDate.now()) }
    var merchant by rememberSaveable { mutableStateOf(initial?.merchant.orEmpty()) }
    var note by rememberSaveable { mutableStateOf(initial?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val visibleCategories = categories.filter { it.type == type && (!it.archived || it.id == categoryId) }
    LaunchedEffect(type, visibleCategories.size) {
        if (visibleCategories.none { it.id == categoryId }) categoryId = visibleCategories.firstOrNull()?.id
    }
    val amountMinor = parseToMinor(amount) ?: 0L
    val canSave = amountMinor > 0 && categoryId != null
    val accent by animateColorAsState(if (type == TxnType.INCOME) Green else ExpenseRed, label = "accent")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (initial == null) "Add transaction" else "Edit transaction",
                style = MaterialTheme.typography.titleLarge,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TxnType.entries.forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = { type = t },
                        shape = SegmentedButtonDefaults.itemShape(index, TxnType.entries.size),
                    ) { Text(if (t == TxnType.EXPENSE) "Expense" else "Income") }
                }
            }
            Text(
                text = "₹" + amount.ifEmpty { "0" },
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = accent,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            AmountKeypad(onKey = { amount = applyKey(amount, it) }, onClear = { amount = "" })

            SheetLabel("Category")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleCategories, key = { it.id }) { category ->
                    CategoryChoice(category, selected = category.id == categoryId) { categoryId = category.id }
                }
            }

            SheetLabel("Paid via")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PaymentMode.entries) { m ->
                    FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m.label) })
                }
            }

            AssistChip(
                onClick = { showDatePicker = true },
                label = { Text(formatFullDate(date)) },
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            )
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it.take(60) },
                label = { Text(if (type == TxnType.INCOME) "From (optional)" else "Merchant (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text("Delete", color = ExpenseRed) }
                }
                Button(
                    onClick = {
                        val zone = ZoneId.systemDefault()
                        val time = initial?.occurredAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() }
                            ?: LocalTime.now()
                        val occurredAt = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
                        onSave(TxnDraft(amountMinor, type, categoryId!!, mode, occurredAt, merchant, note))
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(2f).height(52.dp),
                ) { Text(if (initial == null) "Save" else "Update") }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = it.toLocalDate(ZoneOffset.UTC) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun CategoryChoice(category: CategoryEntity, selected: Boolean, onClick: () -> Unit) {
    val border by animateColorAsState(if (selected) Green else Color.Transparent, label = "catBorder")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        CategoryIcon(category.iconKey, category.colorHex)
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
        )
    }
}
