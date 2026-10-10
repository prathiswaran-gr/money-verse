package com.my.moneyverse.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TxnType { EXPENSE, INCOME }

enum class PaymentMode { UPI, CARD, CASH, NETBANKING, WALLET, OTHER }

enum class TxnSource { MANUAL, NOTIFICATION, SMS, GMAIL }

enum class ReviewState { REVIEWED, UNREVIEWED }

enum class BudgetPeriod { WEEKLY, MONTHLY }

@Entity(
    tableName = "transactions",
    indices = [Index("occurredAt"), Index("categoryId")],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,
    val currency: String = "INR",
    val type: TxnType,
    val categoryId: String,
    val merchant: String? = null,
    val note: String? = null,
    val paymentMode: PaymentMode,
    val occurredAt: Long,
    val zoneId: String,
    val source: TxnSource = TxnSource.MANUAL,
    val reviewState: ReviewState = ReviewState.REVIEWED,
    val groupId: String? = null,
    val dedupeKey: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val type: TxnType,
    val sortOrder: Int,
    val archived: Boolean = false,
    val isSystem: Boolean = false,
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String? = null,
    val limitMinor: Long,
    val period: BudgetPeriod,
    val startDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)
