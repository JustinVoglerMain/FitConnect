package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.model.UserModel


class UserProfileFeedViewModel : BaseFeedViewModel() {
    override val tag: String = "UserProfileViewModel"
    private val _userModel = MutableLiveData<UserModel>()
    val userModel: LiveData<UserModel> get() = _userModel

    override fun removePost(postModel: PostModel) {
        // @TODO - remove this and make it only available in the CurrentUserProfileViewModel
    }

    override fun loadUserFeed(userId: String, isPagination: Boolean) {
        Log.d(tag, "loadUserFeed() called")
        dbTools.getTimelinePosts(userId, this.lastVisiblePost) { posts, newLastVisiblePost ->
            val currentPosts = timeLineFeedPostModelMap.value?.values?.flatten() ?: emptyList()
            if (currentPosts != posts) {
                if (isPagination) {
                    posts.forEach { addPost(it) }
                } else {
                    updatePosts(posts)
                }
                this.lastVisiblePost = newLastVisiblePost
                dbTools.attachTimelineListener(userId) { newPosts ->
                    newPosts.forEach { addPost(it) }
                }
            }
        }
    }

    fun loadUserProfile(profileOwnerId: String, callback: (Boolean) -> Unit) {
        Log.d(tag, "loadUserProfile() called")
        dbTools.getUser(profileOwnerId) { user ->
            if (user == null) {
                Log.d(tag, "Could not load user")
                callback(false)
            } else {
                user.let {
                    Log.d(tag, "Profile loaded correctly")
                    _userModel.value = it
                    callback(true)
                }
            }
        }
    }
}