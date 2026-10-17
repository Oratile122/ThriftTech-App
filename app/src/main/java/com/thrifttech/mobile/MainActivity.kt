package com.thrifttech.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thrifttech.mobile.screens.*
import com.thrifttech.mobile.ui.theme.ThriftTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThriftTechTheme {
                ThriftTechApp()
            }
        }
    }
}

@Composable
fun ThriftTechApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSwitchToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSwitchToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onGoToProducts = { navController.navigate("products") },
                onGoToSell = { navController.navigate("sell") },
                onGoToProfile = { navController.navigate("profile") }
            )
        }

        composable("products") {
            ProductsScreen(onBack = { navController.popBackStack() })
        }

        composable("sell") {
            SellScreen(onSubmitted = { navController.popBackStack() })
        }

        composable("profile") {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}