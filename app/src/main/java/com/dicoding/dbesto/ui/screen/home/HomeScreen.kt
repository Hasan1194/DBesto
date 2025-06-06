package com.dicoding.dbesto.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dicoding.dbesto.di.Injection
import com.dicoding.dbesto.model.MenuItemModel
import com.dicoding.dbesto.ui.ViewModelFactory
import com.dicoding.dbesto.ui.common.UiState
import com.dicoding.dbesto.ui.components.MenuItem
import com.dicoding.dbesto.ui.components.SearchBar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideRepository())
    ),
    navigateToDetail: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState

    when (uiState) {
        is UiState.Loading -> {
            viewModel.getAllMenus()
        }

        is UiState.Success -> {
            HomeContent(
                menu = (uiState as UiState.Success<List<MenuItemModel>>).data,
                query = menuState.query,
                onQueryChange = viewModel::onQueryChange,
                modifier = modifier,
                navigateToDetail = navigateToDetail,
            )
        }

        is UiState.Error -> {

        }
    }
}

@Composable
fun HomeContent(
    menu: List<MenuItemModel>,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navigateToDetail: (String) -> Unit,
) {
    Column {
        SearchBar(query = query, onQueryChange = onQueryChange)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(180.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            items(menu) { data ->
                MenuItem(
                    image = data.menu.image,
                    title = data.menu.title,
                    requiredPoint = data.menu.price,
                    modifier = Modifier.clickable {
                        navigateToDetail(data.menu.documentId)
                    }
                )
            }
        }
    }
}