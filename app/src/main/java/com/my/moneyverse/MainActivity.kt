package com.my.moneyverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.my.moneyverse.ui.budget.BudgetsScreen
import com.my.moneyverse.ui.category.CategoriesScreen
import com.my.moneyverse.ui.home.HomeScreen
import com.my.moneyverse.ui.settings.SettingsScreen
import com.my.moneyverse.ui.theme.Black
import com.my.moneyverse.ui.theme.Border
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.ui.theme.GreenTint
import com.my.moneyverse.ui.theme.MoneyVerseTheme
import com.my.moneyverse.ui.theme.OnSurfaceDim

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Black.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Black.toArgb()),
        )
        setContent {
            MoneyVerseTheme {
                MoneyVerseRoot()
            }
        }
    }
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home),
    BUDGETS("budgets", "Budgets", Icons.Filled.PieChart),
    CATEGORIES("categories", "Categories", Icons.Filled.Category),
    SETTINGS("settings", "Settings", Icons.Filled.Settings),
}

@Composable
private fun MoneyVerseRoot() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Black,
        bottomBar = {
            HorizontalDivider(color = Border)
            NavigationBar(containerColor = Black, tonalElevation = 0.dp) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Green,
                            indicatorColor = GreenTint,
                            selectedTextColor = Green,
                            unselectedIconColor = OnSurfaceDim,
                            unselectedTextColor = OnSurfaceDim,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.HOME.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Tab.HOME.route) { HomeScreen() }
            composable(Tab.BUDGETS.route) { BudgetsScreen() }
            composable(Tab.CATEGORIES.route) { CategoriesScreen() }
            composable(Tab.SETTINGS.route) { SettingsScreen() }
        }
    }
}