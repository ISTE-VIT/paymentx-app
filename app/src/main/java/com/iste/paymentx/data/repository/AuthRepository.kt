package com.iste.paymentx.data.repository

import com.iste.paymentx.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository(private val auth: FirebaseAuth) {

    fun signInWithGoogle(idToken: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        onSuccess(User(it.email, it.displayName, it.uid,false))
                    }
                } else {
                    onFailure(task.exception ?: Exception("Authentication failed"))
                }
            }
    }
    
    fun isUserLoggedIn() = auth.currentUser != null
}