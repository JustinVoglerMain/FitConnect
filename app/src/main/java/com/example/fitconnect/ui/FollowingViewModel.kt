package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitconnect.model.FollowingModel
import com.example.fitconnect.tools.DBTools
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot

private const val TAG = "FriendsListViewModel"

class FollowingViewModel : ViewModel() {
    private val dbTools = DBTools()
    private val _friendsList = MutableLiveData<List<FollowingModel>>(emptyList())
    val friendsList: LiveData<List<FollowingModel>> = _friendsList

    var lastVisibleDocument: DocumentSnapshot? = null
    var isLoading = false
    private var searchQuery = ""

    fun loadFollowing() {
        Log.d(TAG, "loadFollowing() called")
        isLoading = true
        val userId = Firebase.auth.currentUser?.uid

        dbTools.getFollowingList(
            userId.toString(),
            lastVisibleDocument,
            searchQuery
        ) { friends, lastDoc ->
            isLoading = false
            if (friends.isEmpty()) {
                Log.d(TAG, "Following empty")
            } else {
                _friendsList.value = (_friendsList.value ?: emptyList()) + friends
                lastVisibleDocument = lastDoc
            }
        }
    }

    fun removeFollowing(document: DocumentSnapshot, callback: (Boolean) -> Unit) {
        // remove the reference to the friend from the user's subcollection
        document.reference.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Friend deleted successfully")
                // update friends list value to no longer contain the delete friend
                _friendsList.value = _friendsList.value?.filterNot { it.document == document }
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting friend with ${document.id}", e)
                callback(false)
            }
    }

    fun searchFollowing(query: String) {
        searchQuery = query
        loadFollowing()
    }
}