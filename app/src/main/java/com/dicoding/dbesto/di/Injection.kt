package com.dicoding.dbesto.di

import com.dicoding.dbesto.data.FirebaseRepository


object Injection {
    private var repository: FirebaseRepository? = null

    fun provideRepository(): FirebaseRepository {
        return repository ?: FirebaseRepository().also { repository = it }
    }
}