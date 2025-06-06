package com.dicoding.dbesto

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dicoding.dbesto.ui.auth.rememberAuthState
import com.dicoding.dbesto.ui.employee.EmployeeScreen
import com.dicoding.dbesto.ui.navigation.NavigationItem
import com.dicoding.dbesto.ui.navigation.NavigationItems
import com.dicoding.dbesto.ui.navigation.Screen
import com.dicoding.dbesto.ui.screen.cart.CartScreen
import com.dicoding.dbesto.ui.screen.detail.DetailScreen
import com.dicoding.dbesto.ui.screen.home.HomeScreen
import com.dicoding.dbesto.ui.screen.login.LoginScreen
import com.dicoding.dbesto.ui.screen.profile.ProfileScreen
import com.dicoding.dbesto.ui.screen.register.RegisterScreen
import com.dicoding.dbesto.ui.owner.OwnerScreen

@Composable
fun JetRewardApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val authState = rememberAuthState()

    LaunchedEffect(authState.isAuthenticated, authState.isLoading) {
        Log.d("JetRewardApp", "AuthState - isAuthenticated: ${authState.isAuthenticated}, isLoading: ${authState.isLoading}, role: ${authState.userRole}")
    }

    if (authState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (authState.isAuthenticated) {
        when (authState.userRole) {
            "owner" -> Screen.Owner.route
            "employee" -> Screen.History.route
            else -> Screen.Home.route
        }
    } else {
        Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            if (authState.isAuthenticated &&
                currentRoute != Screen.DetailReward.route &&
                currentRoute != Screen.Login.route &&
                currentRoute != Screen.Register.route) {
                RoleBasedBottomBar(
                    navController = navController,
                    userRole = authState.userRole ?: "customer"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        val destination = when (authState.userRole) {
                            "owner" -> Screen.Owner.route
                            "employee" -> Screen.History.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        val destination = when (authState.userRole) {
                            "owner" -> Screen.Owner.route
                            "employee" -> Screen.History.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                if (authState.isAuthenticated && authState.userRole == "customer") {
                    HomeScreen(
                        navigateToDetail = { menuDocumentId ->
                            navController.navigate(Screen.DetailReward.createRoute(menuDocumentId))
                        }
                    )
                } else {
                    NavigateToLogin(navController)
                }
            }

            composable(Screen.Cart.route) {
                if (authState.isAuthenticated && authState.userRole == "customer") {
                    CartScreen()
                } else {
                    NavigateToLogin(navController)
                }
            }

            composable(Screen.Owner.route) {
                if (authState.isAuthenticated && authState.userRole == "owner") {
                    OwnerScreen()
                } else {
                    NavigateToLogin(navController)
                }
            }

            composable(Screen.History.route) {
                if (authState.isAuthenticated && authState.userRole == "employee") {
                    EmployeeScreen()
                } else {
                    NavigateToLogin(navController)
                }
            }

            composable(Screen.Profile.route) {
                if (authState.isAuthenticated) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                } else {
                    NavigateToLogin(navController)
                }
            }

            composable(
                route = Screen.DetailReward.route,
                arguments = listOf(navArgument("menuDocumentId") { type = NavType.StringType }),
            ) {
                if (authState.isAuthenticated && authState.userRole == "customer") {
                    val menuDocumentId = it.arguments?.getString("menuDocumentId") ?: ""
                    DetailScreen(
                        menuDocumentId = menuDocumentId,
                        navigateBack = {
                            navController.navigateUp()
                        },
                        navigateToCart = {
                            navController.popBackStack()
                            navController.navigate(Screen.Cart.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                } else {
                    NavigateToLogin(navController)
                }
            }
        }
    }
}

@Composable
private fun NavigateToLogin(navController: NavHostController) {
    LaunchedEffect(Unit) {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }
}

@Composable
private fun RoleBasedBottomBar(
    navController: NavHostController,
    userRole: String,
    modifier: Modifier = Modifier
) {
    val navigationItems = when (userRole) {
        "owner" -> NavigationItems.ownerItems
        "employee" -> NavigationItems.employeeItems
        else -> NavigationItems.customerItems // customer
    }

    NavigationBar(
        modifier = modifier,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}