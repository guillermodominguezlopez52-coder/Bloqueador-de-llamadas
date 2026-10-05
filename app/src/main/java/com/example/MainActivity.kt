package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.BlacklistScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WhitelistScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra("navigate_to")
        if (target == "history") {
            viewModel.navigateTo(AppScreen.HISTORY)
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    if (!settings.onboardingCompleted) {
        OnboardingScreen(viewModel = viewModel)
    } else {
        // Manejo del botón atrás del sistema para volver al Dashboard antes de salir
        BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HISTORY,
                        onClick = { viewModel.navigateTo(AppScreen.HISTORY) },
                        icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                        label = { Text("Historial") },
                        modifier = Modifier.testTag("nav_history")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.BLACKLIST,
                        onClick = { viewModel.navigateTo(AppScreen.BLACKLIST) },
                        icon = { Icon(Icons.Default.Block, contentDescription = "Bloqueados") },
                        label = { Text("Bloqueados") },
                        modifier = Modifier.testTag("nav_blacklist")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.WHITELIST,
                        onClick = { viewModel.navigateTo(AppScreen.WHITELIST) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Excepciones") },
                        label = { Text("Excepciones") },
                        modifier = Modifier.testTag("nav_whitelist")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                        label = { Text("Ajustes") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppScreen.HISTORY -> HistoryScreen(viewModel = viewModel)
                    AppScreen.BLACKLIST -> BlacklistScreen(viewModel = viewModel)
                    AppScreen.WHITELIST -> WhitelistScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
