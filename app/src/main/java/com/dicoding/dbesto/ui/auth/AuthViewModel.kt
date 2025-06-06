package com.dicoding.dbesto.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dicoding.dbesto.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _authState.value = _authState.value.copy(
            isAuthenticated = user != null,
            user = user,
            isLoading = false
        )

        if (user != null) {
            loadCurrentUserData()
        } else {
            _authState.value = _authState.value.copy(
                userData = null,
                userRole = null
            )
        }
    }

    init {
        val currentUser = auth.currentUser
        _authState.value = AuthState(
            isAuthenticated = currentUser != null,
            user = currentUser,
            isLoading = false
        )

        auth.addAuthStateListener(authStateListener)

        if (currentUser != null) {
            loadCurrentUserData()
        }
    }

    private fun loadCurrentUserData() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()

            result.fold(
                onSuccess = { userData ->
                    val userRole = userData?.get("role") as? String ?: "customer"
                    _authState.value = _authState.value.copy(
                        userData = userData,
                        userRole = userRole
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        error = exception.message ?: "Failed to load user data"
                    )
                }
            )
        }
    }

    fun refreshAuthState() {
        val currentUser = auth.currentUser
        _authState.value = _authState.value.copy(
            isAuthenticated = currentUser != null,
            user = currentUser,
            isLoading = false
        )

        if (currentUser != null) {
            loadCurrentUserData()
        }
    }
}

@Composable
fun rememberAuthState(): AuthState {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.refreshAuthState()
    }

    return authState
}