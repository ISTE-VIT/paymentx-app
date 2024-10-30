package com.iste.paymentx.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iste.paymentx.data.repository.AuthRepository
import com.iste.paymentx.viewmodel.AuthViewModel

class ViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}