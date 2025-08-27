package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.tools.DBTools
import com.google.firebase.firestore.DocumentSnapshot

private const val TAG = "SearchUserViewModel"

class SearchUserViewModel : ViewModel() {
    private val dbTools = DBTools()
    private val _userList = MutableLiveData<List<UserModel>>(emptyList())
    var isLoading: Boolean = false
    val userList: LiveData<List<UserModel>> get() = _userList
    var lastVisibleDocument: DocumentSnapshot? = null
    private var searchQuery = ""

    /**
     * Loads users based on search query given (if any)
     *
     * @param isPagination adds onto the current list if true, else creates new list
     */
    fun loadUsers(isPagination: Boolean = false) {
        isLoading = true
        Log.d(TAG, "loadUsers() called")
        dbTools.getUsersFromSearchQuery(lastVisibleDocument, searchQuery) { users, lastDoc ->
            if (users.isNotEmpty()) {
                Log.d(TAG, "received list of users. Adding to user list")
                if (isPagination) {
                    _userList.value = (_userList.value ?: emptyList()) + users
                } else {
                    _userList.value = users
                }
                lastVisibleDocument = lastDoc
            } else {
                Log.d(TAG, "User list was empty - possible error")
            }
        }
        isLoading = false
    }

    /**
     * Searches for users to add to the current user list
     *
     * @param query - the search query to be used
     */
    fun searchUsers(query: String) {
        Log.d(TAG, "searchUsers() called")
        _userList.value = emptyList()
        searchQuery = query
        lastVisibleDocument = null
        loadUsers()
    }
}