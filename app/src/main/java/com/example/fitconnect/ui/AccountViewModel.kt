package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.tools.DBTools
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private const val TAG = "AccountViewModel"

class AccountViewModel : ViewModel() {
    private var verificationData: MutableLiveData<Boolean> = MutableLiveData()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var dbTools: DBTools = DBTools()
    val verificationStatus: LiveData<Boolean> get() = verificationData

    /**
     * Deletes the user from the firebase Authentication database in the event that
     * a document entry fails in the database
     *
     * @param user
     */
    private fun deleteOnError(user: FirebaseUser) {
        Log.d(TAG, "deleteOnError() called. Check DBTools Log for why")
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Log.d(TAG, "Account deletion successful")
            } else {
                Log.d(
                    TAG,
                    "Account deletion failed ${deleteTask.exception ?: "No exception given"}"
                )
            }
        }
    }

    /**
     * Creates the user's Authentication credentials and adds their information into the database
     *
     * @param username the desired username to be displayed
     * @param userEmail the user's email address
     * @param password the user's password
     * @param confirmationPassword the user's confirmation password
     */
    fun createAccount(
        username: String,
        userEmail: String,
        password: String,
        confirmationPassword: String
    ) {
        Log.d(TAG, "createAccount() called")
        val matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail)
        if (password == confirmationPassword && matcher.matches()) {
            // Create user with Firebase authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully created authentication account")
                        // Create user entry in database
                        val user = auth.currentUser
                        user?.uid?.let { uid ->
                            val newUser = UserModel(uid, username, userEmail)
                            dbTools.addUser(newUser) { success ->
                                if (success) {
                                    verificationData.postValue(true)
                                } else {
                                    // Firebase signs in the user so sign out
                                    auth.signOut()
                                    // delete the user from db
                                    deleteOnError(user)
                                    verificationData.postValue(false)
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Account Creation Error:  ${task.exception?.message}")
                        verificationData.postValue(false)
                    }
                }
        } else {
            Log.d(TAG, "Mismatching passwords or incorrect pattern for email")
            verificationData.postValue(false)
        }
    }
}