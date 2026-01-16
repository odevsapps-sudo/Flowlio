package hu.odevs.utilitymetertracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.odevs.utilitymetertracker.ui.screens.MainScreen
import hu.odevs.utilitymetertracker.ui.screens.ProvidersScreen
import hu.odevs.utilitymetertracker.ui.screens.ReadingsTableScreen
import hu.odevs.utilitymetertracker.ui.screens.FilteredReadingsTableScreen
import hu.odevs.utilitymetertracker.ui.screens.ReadingsListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainScreen(navController = navController)
                }
                composable("providers") {
                    ProvidersScreen(navController = navController)
                }
                composable("readings_table") {
                    ReadingsTableScreen()
                }
                composable("filtered_readings_table") {
                    FilteredReadingsTableScreen()
                }
                composable("reading_list_screen") {
                    ReadingsListScreen() // Biztos, hogy itt van a komponens
                }
            }
        }
    }
}
