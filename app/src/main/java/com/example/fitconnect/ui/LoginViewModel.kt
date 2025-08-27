package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private const val TAG = "LoginViewModel"

class LoginViewModel : ViewModel() {
    private val loginStatusBool = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = loginStatusBool

    fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn() called")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signIn::success")
                    loginStatusBool.value = true
                } else {
                    Log.d(TAG, "signIn::failure")
                    loginStatusBool.value = false
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser // Access the property directly
    }

}