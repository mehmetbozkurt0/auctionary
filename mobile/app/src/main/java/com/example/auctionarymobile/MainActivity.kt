package com.example.auctionarymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auctionarymobile.network.AuthManager
import com.example.auctionarymobile.ui.AppDrawerContent
import com.example.auctionarymobile.ui.AuctionDetailScreen
import com.example.auctionarymobile.ui.AuctionListScreen
import com.example.auctionarymobile.ui.CreateAuctionScreen
import com.example.auctionarymobile.ui.LoginScreen
import com.example.auctionarymobile.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(this)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                // Başlangıç değeri her zaman net bir şekilde Kapalı (Closed)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val userToken by viewModel.userToken.collectAsState()
                val isUserLoggedIn = userToken != null || AuthManager.isLoggedIn()

                val startDestination = if (AuthManager.isLoggedIn()) "list" else "login"

                if (AuthManager.isLoggedIn()) {
                    val savedUsername = AuthManager.getUsername() ?: ""
                    viewModel.restoreSession(savedUsername)
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    // Güvenlik Kilidi: Sadece "login" ekranında değilsek kaydırmaya izin ver
                    gesturesEnabled = currentRoute != "login",
                    drawerContent = {
                        // DİKKAT: Buradaki if(currentRoute != login) şartını kaldırdık!
                        // Menü hep var olacak ama kapalı duracak. Bu sayede "yoktan var olma" yansıması (flash) bitti.
                        AppDrawerContent(
                            username = viewModel.currentUsername.ifEmpty { AuthManager.getUsername() ?: "" },
                            onNavigate = { route ->
                                scope.launch { drawerState.close() }
                                navController.navigate(route)
                            },
                            onLogoutClick = {
                                scope.launch {
                                    // 1. Önce menünün o şık animasyonla tamamen kapanmasını BEKLE
                                    drawerState.close()

                                    // 2. Kapanma bitince verileri temizle ve login ekranına fırlat
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            }
                        )
                    }
                ) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("list") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("list") {
                            AuctionListScreen(
                                viewModel = viewModel,
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                },
                                onAuctionClick = { clickedAuctionId ->
                                    navController.navigate("detail/$clickedAuctionId")
                                },
                                onCreateClick = {
                                    navController.navigate("create")
                                }
                            )
                        }

                        composable("create") {
                            CreateAuctionScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onSuccess = { navController.popBackStack() }
                            )
                        }

                        composable("detail/{auctionId}") { backStackEntry ->
                            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""

                            AuctionDetailScreen(
                                auctionId = auctionId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}