package com.my.moneyverse.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.moneyverse.data.MoneyRepository
import com.my.moneyverse.ui.appViewModel
import com.my.moneyverse.ui.components.Eyebrow
import com.my.moneyverse.ui.components.MvCard
import com.my.moneyverse.ui.components.ScreenHeader
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.ui.theme.OnSurfaceDim
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: MoneyRepository) : ViewModel() {
    suspend fun exportCsv(): String = repo.exportCsv()
    fun clearAll() = viewModelScope.launch { repo.clearAll() }
    fun loadDemoData() = viewModelScope.launch { repo.loadDemoData() }
}

@Composable
fun SettingsScreen() {
    val vm = appViewModel { SettingsViewModel(it) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(eyebrow = "Preferences", title = "Settings")
        SettingsGroup("General") {
            SettingRow(Icons.Filled.CurrencyRupee, "Currency", "Indian Rupee (INR)")
            SettingRow(Icons.Filled.Info, "About", "MoneyVerse v1.0 • Data stays on this device")
        }
        SettingsGroup("Data") {
        SettingRow(Icons.Filled.FileDownload, "Export CSV", "Share all transactions as CSV") {
            scope.launch {
                val csv = vm.exportCsv()
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "MoneyVerse transactions")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                context.startActivity(Intent.createChooser(send, "Export transactions"))
            }
        }
        SettingRow(Icons.Filled.AutoAwesome, "Load demo data", "Add 3 months of sample expenses, income and budgets") {
            vm.loadDemoData()
            Toast.makeText(context, "Demo data loaded", Toast.LENGTH_SHORT).show()
        }
        SettingRow(Icons.Filled.DeleteForever, "Clear all data", "Delete transactions, budgets and custom categories", ExpenseRed) {
            confirmClear = true
        }
        }
        Text(
            "Built with ❤️ in India",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all data?") },
            text = { Text("This permanently deletes everything stored in MoneyVerse on this device.") },
            confirmButton = {
                TextButton(onClick = { vm.clearAll(); confirmClear = false }) { Text("Delete", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = Green,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim) },
        leadingContent = {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Eyebrow(title, color = OnSurfaceDim, modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp))
    MvCard(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), content = content)
}
