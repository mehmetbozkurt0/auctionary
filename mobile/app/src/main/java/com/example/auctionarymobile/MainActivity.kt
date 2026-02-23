package com.example.auctionarymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auctionarymobile.ui.AuctionDetailScreen
import com.example.auctionarymobile.ui.AuctionListScreen
import com.example.auctionarymobile.ui.LoginScreen
import com.example.auctionarymobile.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                NavHost(navController = navController, startDestination = "login") {

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