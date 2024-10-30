package com.iste.paymentx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iste.paymentx.data.model.User
import com.iste.paymentx.data.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun signInWithGoogle(idToken: String) {
        repository.signInWithGoogle(
            idToken,
            onSuccess = { user ->
                _user.postValue(user)
            },
            onFailure = { exception ->
                _error.postValue(exception.message ?: "Sign-in failed")
            }
        )
    }

    fun isUserLoggedIn() = repository.isUserLoggedIn()
}
