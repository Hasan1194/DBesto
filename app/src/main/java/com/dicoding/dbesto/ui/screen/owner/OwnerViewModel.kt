package com.dicoding.dbesto.ui.screen.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.model.MenuItemListModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OwnerViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerState())
    val uiState: StateFlow<OwnerState> = _uiState.asStateFlow()

    init {
        loadMenus()
    }

    fun loadMenus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val menus = repository.getMenus()
                _uiState.value = _uiState.value.copy(
                    menus = menus,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load menus"
                )
            }
        }
    }

    // Fungsi untuk menambah menu baru
    fun addMenu(title: String, description: String, price: String, imageUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val priceInt = price.toIntOrNull() ?: 0
                // Generate menuId baru (bisa menggunakan timestamp atau counter)
                val newMenuId = System.currentTimeMillis().toInt()

                val newMenu = MenuItemListModel(
                    documentId = "", // Kosong untuk menu baru
                    menuId = newMenuId,
                    title = title,
                    description = description,
                    price = priceInt,
                    image = imageUrl
                )

                repository.saveMenu(newMenu)

                _uiState.value = _uiState.value.copy(isLoading = false)

                // Reload menus setelah berhasil menambah
                loadMenus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to add menu"
                )
            }
        }
    }

    // Fungsi untuk mengedit menu
    fun updateMenu(menu: MenuItemListModel, title: String, description: String, price: String, imageUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val priceInt = price.toIntOrNull() ?: 0

                val updatedMenu = menu.copy(
                    title = title,
                    description = description,
                    price = priceInt,
                    image = imageUrl
                )

                repository.saveMenu(updatedMenu)

                _uiState.value = _uiState.value.copy(isLoading = false)

                // Reload menus setelah berhasil mengedit
                loadMenus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update menu"
                )
            }
        }
    }

    fun showDeleteDialog(menu: MenuItemListModel) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            menuToDelete = menu
        )
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            menuToDelete = null
        )
    }

    fun deleteMenu() {
        val menuToDelete = _uiState.value.menuToDelete ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            try {
                repository.deleteMenu(menuToDelete.documentId)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    showDeleteDialog = false,
                    menuToDelete = null
                )
                // Reload menus after successful deletion
                loadMenus()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to delete menu"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
