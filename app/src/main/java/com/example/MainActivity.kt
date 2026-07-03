package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddEditProjectScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProjectDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as RepoMuseApplication
        val repository = app.repository

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                repository = repository,
                                onAddClick = { navController.navigate("addEdit/0") },
                                onProjectClick = { id -> navController.navigate("detail/$id") },
                                onEditClick = { id -> navController.navigate("addEdit/$id") }
                            )
                        }
                        composable("addEdit/{projectId}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
                            AddEditProjectScreen(
                                repository = repository,
                                projectId = projectId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("detail/{projectId}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
                            ProjectDetailScreen(
                                repository = repository,
                                projectId = projectId,
                                onNavigateBack = { navController.popBackStack() },
                                onEditClick = { id -> navController.navigate("addEdit/$id") }
                            )
                        }
                    }
                }
            }
        }
    }
}

