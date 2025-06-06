package com.dicoding.dbesto.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dbesto.data.FirebaseRepository
import com.dicoding.dbesto.ui.screen.cart.CartViewModel
import com.dicoding.dbesto.ui.screen.detail.DetailMenuViewModel
import com.dicoding.dbesto.ui.screen.home.HomeViewModel
import com.dicoding.dbesto.ui.screen.login.LoginViewModel
import com.dicoding.dbesto.ui.screen.register.RegisterViewModel

class ViewModelFactory(private val repository: FirebaseRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(DetailMenuViewModel::class.java)) {
            return DetailMenuViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}