package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.tools.DBTools
import com.google.firebase.firestore.DocumentSnapshot


abstract class BaseFeedViewModel : ViewModel() {
    abstract val tag: String

    // Live data holding the map of posts grouped by user ID
    protected val timeLineFeedPostModelMap =
        MutableLiveData<MutableMap<String, MutableList<PostModel>>>()
            .apply { value = mutableMapOf() }
    protected val dbTools: DBTools = DBTools()
    protected var lastVisiblePost: DocumentSnapshot? = null

    // a map of user posts
    val timelinePosts: LiveData<List<PostModel>>
        get() = timeLineFeedPostModelMap.map { map ->
            map.values.flatten()
        }

    /**
     * Add a post to the user's timeline
     *
     * @param postModel the post being added
     */
    fun addPost(postModel: PostModel) {
        Log.d(tag, "addPost() called")
        val postsMap = timeLineFeedPostModelMap.value ?: mutableMapOf()
        val userPosts = postsMap[postModel.posterUID] ?: mutableListOf()

        val existingPostIndex = userPosts.indexOfFirst { it.postId == postModel.postId }

        if (existingPostIndex != -1) {
            Log.d(tag, "Updating exists posts")
            userPosts[existingPostIndex] = postModel
        } else {
            Log.d(tag, "Adding a new post")
            userPosts.add(postModel)
        }
        postsMap[postModel.posterUID] = userPosts
        timeLineFeedPostModelMap.value = postsMap
    }

    /**
     * Updates the user's posts with the new posts list
     *
     * @param newPosts the list of new posts to use
     */
    fun updatePosts(newPosts: List<PostModel>) {
        Log.d(tag, "updatePosts() called")
        val currentPosts = timeLineFeedPostModelMap.value?.values?.flatten() ?: emptyList()
        if (currentPosts == newPosts) {
            // no new posts to update
            return
        }
        val postsMap = mutableMapOf<String, MutableList<PostModel>>()
        for (post in newPosts) {
            val userPosts = postsMap.getOrPut(post.posterUID) { mutableListOf() }
            userPosts.add(post)
        }
        timeLineFeedPostModelMap.value = postsMap

    }

    /**
     * Remove a post from the user's timeline
     *
     * @param postModel
     */
    abstract fun removePost(postModel: PostModel)

    abstract fun loadUserFeed(userId: String, isPagination: Boolean = false)
}