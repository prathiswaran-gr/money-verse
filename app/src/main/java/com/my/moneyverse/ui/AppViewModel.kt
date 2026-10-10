package com.my.moneyverse.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.my.moneyverse.MoneyVerseApp
import com.my.moneyverse.data.MoneyRepository

/** Creates a ViewModel backed by the app-wide [MoneyRepository]. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (MoneyRepository) -> VM): VM {
    val app = LocalContext.current.applicationContext as MoneyVerseApp
    return viewModel(factory = viewModelFactory { initializer { create(app.repository) } })
}
