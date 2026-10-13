package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.KickoffScreen
import com.example.ui.screens.LineupsScreen
import com.example.ui.screens.LiveMatchScreen
import com.example.ui.screens.MatchDetailScreen
import com.example.ui.screens.MatchReportScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.NewMatchScreen
import com.example.ui.screens.PreMatchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.RwandaScoutTheme
import com.example.ui.theme.ScoutDivider
import com.example.ui.theme.ScoutPrimaryAction
import com.example.ui.theme.ScoutSurface
import com.example.ui.theme.ScoutSurfaceVariant
import com.example.ui.theme.ScoutTextHint
import com.example.ui.theme.ScoutTypographyLabelM
import com.example.ui.viewmodel.ScoutViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScoutViewModel by viewModels {
        ScoutViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val reduceMotion by viewModel.reduceMotion.collectAsState()
            val highContrast by viewModel.highContrast.collectAsState()
            val largerText by viewModel.largerText.collectAsState()

            RwandaScoutTheme(
                reduceMotion = reduceMotion,
                highContrast = highContrast,
                largerText = largerText
            ) {
                ScoutMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ScoutMainApp(viewModel: ScoutViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Show bottom bar on primary tabs
    val showBottomBar = currentDestination == "matches" || currentDestination == "settings"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.height(64.dp),
                    containerColor = ScoutSurface,
                    tonalElevation = 2.dp
                ) {
                    // Matches Tab
                    val isMatchesSelected = currentDestination == "matches"
                    NavigationBarItem(
                        selected = isMatchesSelected,
                        onClick = {
                            navController.navigate("matches") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "Matches tab",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Matches",
                                style = ScoutTypographyLabelM.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isMatchesSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScoutPrimaryAction,
                            selectedTextColor = ScoutPrimaryAction,
                            unselectedIconColor = ScoutTextHint,
                            unselectedTextColor = ScoutTextHint,
                            indicatorColor = ScoutPrimaryAction.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_matches")
                    )

                    // Settings Tab
                    val isSettingsSelected = currentDestination == "settings"
                    NavigationBarItem(
                        selected = isSettingsSelected,
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings tab",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                style = ScoutTypographyLabelM.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSettingsSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScoutPrimaryAction,
                            selectedTextColor = ScoutPrimaryAction,
                            unselectedIconColor = ScoutTextHint,
                            unselectedTextColor = ScoutTextHint,
                            indicatorColor = ScoutPrimaryAction.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "matches",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Matches List
            composable("matches") {
                MatchesScreen(
                    viewModel = viewModel,
                    onNavigateToNewMatch = {
                        navController.navigate("newMatch")
                    },
                    onNavigateToMatchDetail = { matchId ->
                        navController.navigate("matchDetail/$matchId")
                    }
                )
            }

            // 2. New Match
            composable("newMatch") {
                NewMatchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onMatchCreated = { matchId ->
                        navController.navigate("matchDetail/$matchId") {
                            popUpTo("matches")
                        }
                    }
                )
            }

            // 3. Match Detail
            composable(
                route = "matchDetail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                MatchDetailScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLineups = { id -> navController.navigate("lineups/$id") },
                    onNavigateToPreMatch = { id -> navController.navigate("preMatch/$id") },
                    onNavigateToKickoff = { id -> navController.navigate("kickoff/$id") },
                    onNavigateToLiveMatch = { id -> navController.navigate("liveMatch/$id") },
                    onNavigateToReport = { id -> navController.navigate("report/$id") }
                )
            }

            // 4. Lineups
            composable(
                route = "lineups/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                LineupsScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 5. Pre-Match
            composable(
                route = "preMatch/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                PreMatchScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onProceedToKickoff = { id -> navController.navigate("kickoff/$id") },
                    onKickoffStarted = { id ->
                        navController.navigate("liveMatch/$id") {
                            popUpTo("matchDetail/$id")
                        }
                    }
                )
            }

            // 6. Kickoff
            composable(
                route = "kickoff/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                KickoffScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onKickoffStarted = { id ->
                        navController.navigate("liveMatch/$id") {
                            popUpTo("matchDetail/$id")
                        }
                    }
                )
            }

            // 7. Live Match
            composable(
                route = "liveMatch/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                LiveMatchScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReport = { id -> navController.navigate("report/$id") }
                )
            }

            // 8. Report
            composable(
                route = "report/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getLong("id") ?: 0L
                MatchReportScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 9. Settings
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onSampleMatchCreated = { id ->
                        navController.navigate("matchDetail/$id")
                    }
                )
            }
        }
    }
}
