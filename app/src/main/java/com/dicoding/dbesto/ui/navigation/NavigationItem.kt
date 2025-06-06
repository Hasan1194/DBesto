package com.dicoding.dbesto.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val roles: List<String> = listOf("customer", "owner", "employee")
)

object NavigationItems {
    val customerItems = listOf(
        NavigationItem(
            title = "Home",
            icon = Icons.Default.Home,
            screen = Screen.Home,
            roles = listOf("customer")
        ),
        NavigationItem(
            title = "Cart",
            icon = Icons.Default.ShoppingCart,
            screen = Screen.Cart,
            roles = listOf("customer")
        ),
        NavigationItem(
            title = "Profile",
            icon = Icons.Default.AccountCircle,
            screen = Screen.Profile,
            roles = listOf("customer", "owner", "employee")
        )
    )

    val ownerItems = listOf(
        NavigationItem(
            title = "Kelola Menu",
            icon = Icons.Default.RestaurantMenu,
            screen = Screen.Owner,
            roles = listOf("owner")
        ),
        NavigationItem(
            title = "Profile",
            icon = Icons.Default.AccountCircle,
            screen = Screen.Profile,
            roles = listOf("customer", "owner", "employee")
        )
    )

    val employeeItems = listOf(
        NavigationItem(
            title = "History",
            icon = Icons.Default.History,
            screen = Screen.History,
            roles = listOf("employee")
        ),
        NavigationItem(
            title = "Profile",
            icon = Icons.Default.AccountCircle,
            screen = Screen.Profile,
            roles = listOf("customer", "owner", "employee")
        )
    )
}

