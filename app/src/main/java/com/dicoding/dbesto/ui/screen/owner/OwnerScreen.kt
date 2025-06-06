package com.dicoding.dbesto.ui.owner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dicoding.dbesto.model.MenuItemListModel
import com.dicoding.dbesto.ui.components.MenuItemCard
import com.dicoding.dbesto.ui.screen.owner.OwnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerScreen(
    viewModel: OwnerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingMenu by remember { mutableStateOf<MenuItemListModel?>(null) }

    var menuTitle by remember { mutableStateOf("") }
    var menuDescription by remember { mutableStateOf("") }
    var menuPrice by remember { mutableStateOf("") }
    var menuImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            menuImageUrl = it.toString()
        }
    }

    fun resetForm() {
        menuTitle = ""
        menuDescription = ""
        menuPrice = ""
        menuImageUrl = ""
        selectedImageUri = null
        editingMenu = null
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kelola Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetForm()
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Menu"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.menus.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada menu",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap tombol + untuk menambah menu baru",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.menus) { menu ->
                            MenuItemCard(
                                menu = menu,
                                onEditClick = {
                                    editingMenu = menu
                                    menuTitle = menu.title
                                    menuDescription = menu.description ?: ""
                                    menuPrice = menu.price.toString()
                                    menuImageUrl = menu.image ?: ""
                                    selectedImageUri = null
                                    showEditDialog = true
                                },
                                onDeleteClick = { viewModel.showDeleteDialog(menu) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                resetForm()
            },
            title = { Text("Tambah Menu Baru") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = menuTitle,
                        onValueChange = { menuTitle = it },
                        label = { Text("Nama Menu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = menuDescription,
                        onValueChange = { menuDescription = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = menuPrice,
                        onValueChange = { menuPrice = it },
                        label = { Text("Harga") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = menuImageUrl,
                                onValueChange = { menuImageUrl = it },
                                label = { Text("URL Gambar") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pilih Gambar"
                                )
                            }
                        }

                        selectedImageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .build(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (menuTitle.isNotBlank() && menuPrice.isNotBlank()) {
                            viewModel.addMenu(menuTitle, menuDescription, menuPrice, menuImageUrl)
                            showAddDialog = false
                            resetForm()
                        }
                    },
                    enabled = menuTitle.isNotBlank() && menuPrice.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Tambah")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        resetForm()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Batal")
                }
            }
        )
    }

    if (showEditDialog && editingMenu != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                resetForm()
            },
            title = { Text("Edit Menu") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = menuTitle,
                        onValueChange = { menuTitle = it },
                        label = { Text("Nama Menu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = menuDescription,
                        onValueChange = { menuDescription = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = menuPrice,
                        onValueChange = { menuPrice = it },
                        label = { Text("Harga") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = menuImageUrl,
                                onValueChange = { menuImageUrl = it },
                                label = { Text("URL Gambar") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pilih Gambar"
                                )
                            }
                        }

                        val imageToShow = selectedImageUri?.toString() ?: menuImageUrl
                        if (imageToShow.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageToShow)
                                    .build(),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (menuTitle.isNotBlank() && menuPrice.isNotBlank()) {
                            viewModel.updateMenu(editingMenu!!, menuTitle, menuDescription, menuPrice, menuImageUrl)
                            showEditDialog = false
                            resetForm()
                        }
                    },
                    enabled = menuTitle.isNotBlank() && menuPrice.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Simpan")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        resetForm()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Batal")
                }
            }
        )
    }

    if (uiState.showDeleteDialog && uiState.menuToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Konfirmasi Hapus") },
            text = {
                Text("Apakah Anda yakin ingin menghapus menu \"${uiState.menuToDelete!!.title}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteMenu() },
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Hapus")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteDialog() },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Batal")
                }
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            android.util.Log.e("OwnerScreen", "Error: $error")
        }
    }
}