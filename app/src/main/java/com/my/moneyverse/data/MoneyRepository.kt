package com.my.moneyverse.data

import androidx.room.withTransaction
import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.data.db.CategoryEntity
import com.my.moneyverse.data.db.MoneyVerseDatabase
import com.my.moneyverse.data.db.PaymentMode
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import com.my.moneyverse.util.uuidV7
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId

class MoneyRepository(private val db: MoneyVerseDatabase) {
    private val txnDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val budgetDao = db.budgetDao()

    val transactions: Flow<List<TransactionEntity>> = txnDao.observeAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeAll()

    suspend fun getTransaction(id: String): TransactionEntity? = txnDao.getById(id)

    suspend fun saveTransaction(
        id: String?,
        amountMinor: Long,
        type: TxnType,
        categoryId: String,
        paymentMode: PaymentMode,
        occurredAt: Long,
        merchant: String?,
        note: String?,
    ) {
        val now = System.currentTimeMillis()
        val existing = id?.let { txnDao.getById(it) }
        val txn = TransactionEntity(
            id = existing?.id ?: uuidV7(now),
            amountMinor = amountMinor,
            type = type,
            categoryId = categoryId,
            merchant = merchant?.trim()?.takeIf { it.isNotEmpty() },
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            paymentMode = paymentMode,
            occurredAt = occurredAt,
            zoneId = ZoneId.systemDefault().id,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        txnDao.upsert(txn)
    }

    suspend fun deleteTransaction(id: String) = txnDao.softDelete(id, System.currentTimeMillis())

    suspend fun restoreTransaction(id: String) = txnDao.restore(id, System.currentTimeMillis())

    suspend fun saveCategory(existing: CategoryEntity?, name: String, iconKey: String, colorHex: String, type: TxnType) {
        val category = existing?.copy(name = name.trim(), iconKey = iconKey, colorHex = colorHex, type = type)
            ?: CategoryEntity(
                id = uuidV7(),
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                type = type,
                sortOrder = categoryDao.maxSortOrder() + 1,
            )
        categoryDao.upsert(category)
    }

    suspend fun setCategoryArchived(id: String, archived: Boolean) = categoryDao.setArchived(id, archived)

    suspend fun saveBudget(existing: BudgetEntity?, categoryId: String?, limitMinor: Long, period: BudgetPeriod) {
        val now = System.currentTimeMillis()
        val budget = existing?.copy(categoryId = categoryId, limitMinor = limitMinor, period = period, updatedAt = now)
            ?: BudgetEntity(
                id = uuidV7(now),
                categoryId = categoryId,
                limitMinor = limitMinor,
                period = period,
                startDate = now,
                createdAt = now,
                updatedAt = now,
            )
        budgetDao.upsert(budget)
    }

    suspend fun deleteBudget(id: String) = budgetDao.softDelete(id, System.currentTimeMillis())

    /** First launch: default categories plus the demo profile. */
    suspend fun seedIfEmpty() = db.withTransaction {
        if (categoryDao.count() == 0) {
            categoryDao.upsertAll(DefaultCategories.all)
            insertDemoData()
        }
    }

    suspend fun loadDemoData() = db.withTransaction {
        categoryDao.upsertAll(DefaultCategories.all)
        txnDao.clearDemo()
        insertDemoData()
    }

    private suspend fun insertDemoData() {
        txnDao.upsertAll(DemoData.transactions())
        budgetDao.upsertAll(DemoData.budgets())
    }

    /** Builds a CSV of all live transactions. */
    suspend fun exportCsv(): String {
        val (names, txns) = db.withTransaction {
            categoryDao.getAll().associate { it.id to it.name } to txnDao.getAll()
        }
        val sb = StringBuilder("date,type,amount,currency,category,payment_mode,merchant,note\n")
        txns.forEach { t ->
            val cells = listOf(
                Instant.ofEpochMilli(t.occurredAt).toString(),
                t.type.name,
                "%d.%02d".format(t.amountMinor / 100, t.amountMinor % 100),
                t.currency,
                names[t.categoryId] ?: t.categoryId,
                t.paymentMode.name,
                t.merchant.orEmpty(),
                t.note.orEmpty(),
            )
            sb.append(cells.joinToString(",") { csvEscape(it) }).append('\n')
        }
        return sb.toString()
    }

    suspend fun clearAll() = db.withTransaction {
        txnDao.clear()
        budgetDao.clear()
        categoryDao.clear()
        categoryDao.upsertAll(DefaultCategories.all)
    }

    private fun csvEscape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) "\"${value.replace("\"", "\"\"")}\"" else value
}
