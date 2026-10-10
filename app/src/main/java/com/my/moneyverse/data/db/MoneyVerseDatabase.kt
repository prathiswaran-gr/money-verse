package com.my.moneyverse.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MoneyVerseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        fun build(context: Context): MoneyVerseDatabase =
            Room.databaseBuilder(context, MoneyVerseDatabase::class.java, "moneyverse.db").build()
    }
}
