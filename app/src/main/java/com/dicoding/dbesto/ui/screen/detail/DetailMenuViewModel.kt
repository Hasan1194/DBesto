package com.dicoding.dbesto.ui.screen.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.model.MenuItemListModel
import com.dicoding.dbesto.model.MenuItemModel
import com.dicoding.dbesto.ui.common.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailMenuViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<MenuItemModel>> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<MenuItemModel>>
        get() = _uiState

    fun getMenuById(menuId: String) {
        viewModelScope.launch {
            try {
                val menu = repository.getMenuById(menuId)
                _uiState.value = UiState.Success(
                    MenuItemModel(
                        menu = menu,
                        count = 1
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun addToCartAndNavigate(menu: MenuItemListModel, count: Int, onComplete: () -> Unit) {
        try {
            repeat(count) {
                repository.addToCart(menu.menuId.toLong())
            }
            delay(100)

            onComplete()
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error adding to cart", e)
        }
    }
}