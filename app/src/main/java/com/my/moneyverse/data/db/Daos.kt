package com.my.moneyverse.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY occurredAt DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Upsert
    suspend fun upsert(txn: TransactionEntity)

    @Upsert
    suspend fun upsertAll(txns: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id LIKE 'demo_%'")
    suspend fun clearDemo()

    @Query("UPDATE transactions SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("UPDATE transactions SET deletedAt = NULL, updatedAt = :now WHERE id = :id")
    suspend fun restore(id: String, now: Long)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY archived ASC, sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM categories")
    suspend fun maxSortOrder(): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("UPDATE categories SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("DELETE FROM categories")
    suspend fun clear()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE deletedAt IS NULL ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Upsert
    suspend fun upsertAll(budgets: List<BudgetEntity>)

    @Query("UPDATE budgets SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("DELETE FROM budgets")
    suspend fun clear()
}
