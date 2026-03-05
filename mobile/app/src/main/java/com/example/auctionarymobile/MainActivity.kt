package com.example.auctionarymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auctionarymobile.network.AuthManager
import com.example.auctionarymobile.ui.AuctionDetailScreen
import com.example.auctionarymobile.ui.AuctionListScreen
import com.example.auctionarymobile.ui.LoginScreen
import com.example.auctionarymobile.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(this)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                val startDestination = if (AuthManager.isLoggedIn()) "list" else "login"

                if (AuthManager.isLoggedIn()) {
                    val savedUsername = AuthManager.getUsername() ?: ""
                    viewModel. restoreSession(savedUsername)
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("list") {
                                    popUpTo("login") {inclusive = true}
                                }
                            }
                        )
                    }

                    composable("list") {
                        AuctionListScreen(
                            viewModel = viewModel,
                            onAuctionClick = { clickedAuctionId ->
                                navController.navigate("detail/$clickedAuctionId")
                            }
                        )
                    }

                    composable("detail/{auctionId}") { backStackEntry ->
                        val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""

                        AuctionDetailScreen(
                            auctionId = auctionId,
                            viewModel = viewModel,
                            onBackClick = {navController.popBackStack()}
                        )
                    }
                }
            }
        }
    }
}