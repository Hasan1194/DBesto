package com.dicoding.dbesto.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.ui.common.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        _uiState.value = UiState.Success("")
    }

    fun updateEmail(email: String) {
        _loginState.value = _loginState.value.copy(
            email = email,
            errorMessage = ""
        )
    }

    fun updatePassword(password: String) {
        _loginState.value = _loginState.value.copy(
            password = password,
            errorMessage = ""
        )
    }

    fun login() {
        val currentState = _loginState.value

        if (!validateInput(currentState)) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                _loginState.value = currentState.copy(isLoading = true, errorMessage = "")

                val result = auth.signInWithEmailAndPassword(
                    currentState.email,
                    currentState.password
                ).await()

                if (result.user != null) {
                    _uiState.value = UiState.Success("Login berhasil")
                } else {
                    _loginState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Login gagal"
                    )
                    _uiState.value = UiState.Error("Login gagal")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("password is invalid") == true -> "Password salah"
                    e.message?.contains("no user record") == true -> "Email tidak terdaftar"
                    e.message?.contains("badly formatted") == true -> "Format email tidak valid"
                    e.message?.contains("network error") == true -> "Periksa koneksi internet"
                    else -> "Terjadi kesalahan: ${e.message}"
                }

                _loginState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }

    private fun validateInput(state: LoginState): Boolean {
        return when {
            state.email.isEmpty() -> {
                _loginState.value = state.copy(errorMessage = "Email tidak boleh kosong")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _loginState.value = state.copy(errorMessage = "Format email tidak valid")
                false
            }
            state.password.isEmpty() -> {
                _loginState.value = state.copy(errorMessage = "Password tidak boleh kosong")
                false
            }
            state.password.length < 6 -> {
                _loginState.value = state.copy(errorMessage = "Password minimal 6 karakter")
                false
            }
            else -> true
        }
    }
}