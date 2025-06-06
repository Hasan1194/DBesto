package com.dicoding.dbesto.ui.screen.cart

import com.dicoding.dbesto.model.MenuItemModel

data class CartState(
    val menu: List<MenuItemModel> = emptyList(),
    val totalRequiredPoint: Int = 0
)