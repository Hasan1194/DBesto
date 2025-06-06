package com.dicoding.dbesto.ui.screen.owner

import com.dicoding.dbesto.model.MenuItemListModel

data class OwnerState(
    val menus: List<MenuItemListModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val menuToDelete: MenuItemListModel? = null
)