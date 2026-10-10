package com.my.moneyverse.ui.category

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.ui.components.CategoryIcon
import com.my.moneyverse.ui.components.parseHexColor

private val emojis = listOf(
    "🍔", "☕", "🛒", "🚕", "⛽", "🛍️", "💡", "📱", "🏠", "🎬", "🎮", "💊",
    "🏋️", "📚", "✈️", "🐶", "👶", "🎁", "💼", "📈", "💰", "🧾", "🔧", "📦",
)
private val colors = listOf(
    "#00E676", "#00C853", "#66BB6A", "#26A69A", "#29B6F6", "#42A5F5",
    "#5C6BC0", "#AB47BC", "#EC407A", "#FF5252", "#FF7043", "#FFCA28",
)

@Composable
fun CategoryDialog(
    initial: CategoryEntity?,
    defaultType: TxnType,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorHex: String, type: TxnType) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var emoji by rememberSaveable { mutableStateOf(initial?.iconKey ?: emojis.first()) }
    var color by rememberSaveable { mutableStateOf(initial?.colorHex ?: colors.first()) }
    var type by rememberSaveable { mutableStateOf(initial?.type ?: defaultType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CategoryIcon(emoji, color, size = 64.dp)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    TxnType.entries.forEachIndexed { i, t ->
                        SegmentedButton(
                            selected = type == t,
                            onClick = { type = t },
                            shape = SegmentedButtonDefaults.itemShape(i, TxnType.entries.size),
                        ) { Text(if (t == TxnType.EXPENSE) "Expense" else "Income") }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emojis) { e ->
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (e == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center,
                        ) { Text(e, fontSize = 20.sp) }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { hex ->
                        val ring by animateDpAsState(if (hex == color) 3.dp else 0.dp, label = "ring")
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(ring, if (hex == color) Color.White else Color.Transparent, CircleShape)
                                .clickable { color = hex },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, emoji, color, type) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
