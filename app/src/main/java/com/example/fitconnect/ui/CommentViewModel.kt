package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitconnect.model.CommentModel
import com.example.fitconnect.tools.DBTools
import com.google.firebase.firestore.DocumentSnapshot

private const val TAG = "CommentViewModel"

class CommentViewModel : ViewModel() {
    private val dbTools = DBTools() // Directly initialize DBTools here

    private val _comments = MutableLiveData<List<CommentModel>>()
    val comments: LiveData<List<CommentModel>> get() = _comments

    private var lastVisibleComment: DocumentSnapshot? = null
    private var isLoading = false

    /**
     * Load the initial set of comments for a post
     */
    fun loadComments(postId: String) {
        dbTools.getComments(postId) { commentList ->
            _comments.value = commentList
        }
    }

    /**
     * Add a new comment to the post
     */
    fun addComment(postId: String, userId: String, commentText: String) {
        dbTools.addComment(postId, userId, commentText) { success ->
            if (success) {
                loadComments(postId)  // Reload comments after adding
            }
        }
    }

    /**
     * Deletes a comment from the post
     */
    fun deleteComment(postId: String, commentId: String) {
        dbTools.deleteComment(postId, commentId) { success ->
            if (success) {
                Log.d(TAG, "Reloading comments after delete.")
                loadComments(postId)
            }
        }
    }

    fun updateComment(postId: String, commentId: String, updatedText: String) {
        dbTools.updateComment(postId, commentId, updatedText) { success ->
            if (success) {
                Log.d(TAG, "Reloading comments after update.")
                loadComments(postId)
            }
        }
    }
}
