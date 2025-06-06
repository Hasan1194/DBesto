package com.dicoding.dbesto.ui.screen.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val uiState: StateFlow<UiState<String>> = _uiState

    private val _isRegistrationComplete = MutableStateFlow(false)
    val isRegistrationComplete: StateFlow<Boolean> = _isRegistrationComplete

    fun updateName(name: String) {
        _registerState.value = _registerState.value.copy(name = name, errorMessage = "")
    }

    fun updateEmail(email: String) {
        _registerState.value = _registerState.value.copy(email = email, errorMessage = "")
    }

    fun updatePhone(phone: String) {
        _registerState.value = _registerState.value.copy(phone = phone, errorMessage = "")
    }

    fun updatePassword(password: String) {
        _registerState.value = _registerState.value.copy(password = password, errorMessage = "")
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _registerState.value = _registerState.value.copy(confirmPassword = confirmPassword, errorMessage = "")
    }

    fun updateRole(role: UserRole) {
        _registerState.value = _registerState.value.copy(role = role, errorMessage = "")
    }

    fun register() {
        val state = _registerState.value

        if (state.name.isBlank()) {
            _registerState.value = state.copy(errorMessage = "Nama tidak boleh kosong")
            return
        }

        if (state.email.isBlank()) {
            _registerState.value = state.copy(errorMessage = "Email tidak boleh kosong")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _registerState.value = state.copy(errorMessage = "Format email tidak valid")
            return
        }

        if (state.phone.isBlank()) {
            _registerState.value = state.copy(errorMessage = "Nomor telepon tidak boleh kosong")
            return
        }

        if (state.password.length < 6) {
            _registerState.value = state.copy(errorMessage = "Password minimal 6 karakter")
            return
        }

        if (state.password != state.confirmPassword) {
            _registerState.value = state.copy(errorMessage = "Password tidak sama")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val userData = mapOf(
                    "name" to state.name,
                    "email" to state.email,
                    "phone" to state.phone,
                    "role" to state.role.value,
                    "createdAt" to System.currentTimeMillis()
                )

                repository.registerUser(userData, state.password)
                _uiState.value = UiState.Success("Registration successful")
                _isRegistrationComplete.value = true
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Registration failed")
                _registerState.value = state.copy(errorMessage = e.message ?: "Registration failed")
            }
        }
    }

    fun resetRegistrationState() {
        _isRegistrationComplete.value = false
    }
}