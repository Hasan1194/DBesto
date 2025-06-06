package com.dicoding.dbesto.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Owner : Screen("owner")
    object History : Screen("history")
    object DetailReward : Screen("detail_reward/{menuDocumentId}") {
        fun createRoute(menuDocumentId: String) = "detail_reward/$menuDocumentId"
    }
}
