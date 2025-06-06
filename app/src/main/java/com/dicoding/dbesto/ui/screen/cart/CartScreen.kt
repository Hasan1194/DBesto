package com.dicoding.dbesto.ui.screen.cart

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dicoding.dbesto.R
import com.dicoding.dbesto.di.Injection
import com.dicoding.dbesto.ui.ViewModelFactory
import com.dicoding.dbesto.ui.common.UiState
import com.dicoding.dbesto.ui.components.CartItem
import com.dicoding.dbesto.ui.components.OrderButton

@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(
        factory = ViewModelFactory(
            Injection.provideRepository()
        )
    ),
    onNavigateBack: (() -> Unit)? = null,
) {
    LaunchedEffect(Unit) {
        viewModel.getAddedOrderMenu()
    }

    val uiState by viewModel.uiState.collectAsState(initial = UiState.Loading)
    val showDialog by viewModel.showSuccessDialog.collectAsState()

    when (uiState) {
        is UiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading cart...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        is UiState.Success -> {
            CartContent(
                state = (uiState as UiState.Success<CartState>).data,
                onProductCountChanged = { menuId, count ->
                    viewModel.updateOrderMenu(menuId, count)
                },
                onOrderButtonClicked = {
                    viewModel.submitOrder()
                }
            )

            if (showDialog) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.dismissDialog()
                        onNavigateBack?.invoke()
                    }
                )
            }
        }

        is UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error: ${(uiState as UiState.Error).errorMessage}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        title = {
            Text(
                text = "🎉 Berhasil!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = "Pesanan Anda berhasil dibuat!\n\nPesanan akan langsung diantar ketika siap. Terima kasih telah berbelanja!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartState,
    onProductCountChanged: (id: Long, count: Int) -> Unit,
    onOrderButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.menu_cart),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        )

        if (state.menu.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Your cart is empty",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Add some items to get started!",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(weight = 1f)
            ) {
                items(state.menu, key = { it.menu.menuId }) { item ->
                    CartItem(
                        menuId = item.menu.menuId.toLong(),
                        image = item.menu.image,
                        title = item.menu.title,
                        totalPoint = item.menu.price * item.count,
                        count = item.count,
                        onProductCountChanged = onProductCountChanged,
                    )
                    HorizontalDivider()
                }
            }
            OrderButton(
                text = stringResource(R.string.total_order, state.totalRequiredPoint),
                enabled = state.menu.isNotEmpty(),
                onClick = {
                    onOrderButtonClicked()
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
