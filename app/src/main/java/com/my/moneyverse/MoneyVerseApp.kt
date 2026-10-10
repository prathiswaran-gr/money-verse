package com.my.moneyverse

import android.app.Application
import com.my.moneyverse.data.MoneyRepository
import com.my.moneyverse.data.db.MoneyVerseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoneyVerseApp : Application() {
    lateinit var repository: MoneyRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = MoneyRepository(MoneyVerseDatabase.build(this))
        appScope.launch { repository.seedIfEmpty() }
    }
}
