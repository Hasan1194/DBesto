package com.dicoding.dbesto.ui.auth

import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: FirebaseUser? = null,
    val isLoading: Boolean = true,
    val userData: Map<String, Any>? = null,
    val userRole: String? = null,
    val error: String? = null
)