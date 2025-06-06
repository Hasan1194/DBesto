package com.dicoding.dbesto.ui.screen.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.model.MenuItemModel
import com.dicoding.dbesto.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<List<MenuItemModel>>> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<List<MenuItemModel>>>
        get() = _uiState

    private val _menuState = mutableStateOf(HomeState())
    val menuState: State<HomeState> = _menuState

    private var originalMenus: List<MenuItemModel> = emptyList()

    fun getAllMenus() {
        viewModelScope.launch {
            try {
                val menus = repository.getMenus()
                val orderMenus = menus.map { MenuItemModel(it, 0) }
                originalMenus = orderMenus
                _uiState.value = UiState.Success(orderMenus)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message.toString())
            }
        }
    }

    private fun searchMenu(query: String) {
        val filteredMenus = if (query.isEmpty()) {
            originalMenus
        } else {
            originalMenus.filter { menuItem ->
                menuItem.menu.title.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = UiState.Success(filteredMenus)
    }

    fun onQueryChange(query: String) {
        _menuState.value = _menuState.value.copy(query = query)
        searchMenu(query)
    }
}