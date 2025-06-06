package com.dicoding.dbesto.ui.screen.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<CartState>> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<CartState>>
        get() = _uiState

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean>
        get() = _showSuccessDialog

    fun dismissDialog() {
        _showSuccessDialog.value = false
    }

    fun getAddedOrderMenu() {
        viewModelScope.launch {
            try {
                val orderMenus = repository.getOrderMenus()
                Log.d("CartViewModel", "Retrieved ${orderMenus.size} items from repository")
                orderMenus.forEachIndexed { index, item ->
                    Log.d("CartViewModel", "Item $index: ${item.menu.title} x${item.count}")
                }

                val totalPoint = orderMenus.sumOf { it.menu.price * it.count }
                _uiState.value = UiState.Success(CartState(orderMenus, totalPoint))
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error getting cart items", e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateOrderMenu(menuId: Long, count: Int) {
        viewModelScope.launch {
            try {
                val success = repository.updateOrderMenu(menuId, count)
                if (success) {
                    getAddedOrderMenu()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update order")
            }
        }
    }

    fun submitOrder() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is UiState.Success) {
                    val orderData = mapOf(
                        "menus" to currentState.data.menu.map { orderMenu ->
                            mapOf(
                                "menuId" to orderMenu.menu.documentId,
                                "title" to orderMenu.menu.title,
                                "price" to orderMenu.menu.price,
                                "count" to orderMenu.count,
                                "totalPrice" to (orderMenu.menu.price * orderMenu.count)
                            )
                        },
                        "totalAllPrice" to currentState.data.totalRequiredPoint,
                        "timestamp" to System.currentTimeMillis()
                    )
                    repository.submitOrder(orderData)
                    _showSuccessDialog.value = true
                    repository.clearCart()
                    getAddedOrderMenu()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to submit order")
            }
        }
    }
}