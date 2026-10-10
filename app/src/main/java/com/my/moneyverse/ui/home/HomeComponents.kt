package com.my.moneyverse.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.my.moneyverse.data.BudgetProgress
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.PaymentMode
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.ui.components.AnimatedMoney
import com.my.moneyverse.ui.components.BudgetBar
import com.my.moneyverse.ui.components.CategoryIcon
import com.my.moneyverse.ui.components.LogoMark
import com.my.moneyverse.ui.components.MvCard
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.ui.theme.OnSurfaceDim
import com.my.moneyverse.ui.theme.SummaryGradient
import com.my.moneyverse.util.formatMoney
import com.my.moneyverse.util.formatMonth
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeHeader() {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("$greeting 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatMonth(LocalDate.now()), style = MaterialTheme.typography.headlineSmall)
        }
        LogoMark()
    }
}

@Composable
fun SummaryCard(spentMinor: Long, incomeMinor: Long, topBudget: BudgetProgress?) {
    MvCard(background = SummaryGradient, borderColor = Green.copy(alpha = 0.12f)) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "SPENT THIS MONTH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedMoney(
                spentMinor,
                style = MaterialTheme.typography.displaySmall,
                color = Green,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            val balance = incomeMinor - spentMinor
            Text(
                buildString {
                    append("Income: ${formatMoney(incomeMinor, false)}")
                    append("  •  Balance: ${formatMoney(balance, false)}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (topBudget != null) {
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${topBudget.title} budget • ${(topBudget.fraction * 100).toInt()}% used",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "${formatMoney(topBudget.spentMinor, false)} / ${formatMoney(topBudget.budget.limitMinor, false)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceDim,
                    )
                }
                BudgetBar(topBudget.fraction, Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun TransactionRow(txn: TransactionEntity, category: CategoryEntity?, onClick: () -> Unit) {
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(category?.iconKey ?: "📦", category?.colorHex ?: "#9E9E9E")
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    txn.merchant ?: category?.name ?: "Transaction",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(category?.name?.takeIf { txn.merchant != null }, txn.paymentMode.label, txn.note)
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val income = txn.type == TxnType.INCOME
            Text(
                (if (income) "+" else "−") + formatMoney(txn.amountMinor),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (income) Green else ExpenseRed,
            )
        }
        HorizontalDivider(Modifier.padding(start = 72.dp, end = 20.dp), color = Color.White.copy(alpha = 0.04f))
    }
}

val PaymentMode.label: String
    get() = when (this) {
        PaymentMode.UPI -> "UPI"
        PaymentMode.CARD -> "Card"
        PaymentMode.CASH -> "Cash"
        PaymentMode.NETBANKING -> "Net banking"
        PaymentMode.WALLET -> "Wallet"
        PaymentMode.OTHER -> "Other"
    }
