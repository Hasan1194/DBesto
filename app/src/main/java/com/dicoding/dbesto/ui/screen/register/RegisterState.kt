package com.dicoding.dbesto.ui.screen.register

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

enum class UserRole(val displayName: String, val value: String) {
    OWNER("Owner", "owner"),
    EMPLOYEE("Karyawan", "employee"),
    CUSTOMER("Customer", "customer")
}