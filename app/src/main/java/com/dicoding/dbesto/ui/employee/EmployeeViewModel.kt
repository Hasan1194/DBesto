package com.dicoding.dbesto.ui.screen.employee

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.model.OrderModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployeeViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderModel>>(emptyList())
    val orders: StateFlow<List<OrderModel>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val ordersList = repository.getOrders()
                _orders.value = ordersList.sortedByDescending { it.timestamp }
                Log.d("EmployeeViewModel", "Loaded ${ordersList.size} orders")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data pesanan: ${e.message}"
                Log.e("EmployeeViewModel", "Error loading orders", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markOrderAsCompleted(orderId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, "completed")
                loadOrders() // Refresh the list
                Log.d("EmployeeViewModel", "Order $orderId marked as completed")
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengupdate status pesanan: ${e.message}"
                Log.e("EmployeeViewModel", "Error updating order status", e)
            }
        }
    }
}