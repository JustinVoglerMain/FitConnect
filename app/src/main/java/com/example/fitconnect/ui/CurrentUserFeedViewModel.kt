package com.example.fitconnect.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fitconnect.model.PostModel
import com.google.firebase.firestore.DocumentSnapshot

/**
 * The viewmodel for a user's timeline
 *
 */
class CurrentUserFeedViewModel : BaseFeedViewModel() {
    override val tag: String = "CurrentUserFeedViewModel"
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    override fun removePost(postModel: PostModel) {
        if (!postModel.isCurrentUser) {
            return
        }
        val postsMap = timeLineFeedPostModelMap.value ?: mutableMapOf()
        val userPosts = postsMap[postModel.posterUID] ?: mutableListOf()

        if (!userPosts.any { it.postId == postModel.postId }) {
            Log.e(
                tag,
                String.format(
                    "removePost::ERROR does not contain post with id %s",
                    postModel.postId
                )
            )
            return
        }

        userPosts.removeAll { it.postId == postModel.postId }
        postsMap[postModel.posterUID] = userPosts
        timeLineFeedPostModelMap.value = postsMap
    }

    override fun loadUserFeed(userId: String, isPagination: Boolean) {
        Log.d(tag, "loadUserFeed() called")
        _isLoading.value = true
        // Current posts
        val currentPosts = timeLineFeedPostModelMap.value?.values?.flatten() ?: emptyList()

        val newPosts = mutableListOf<PostModel>()
        var combinedLastVisiblePost: DocumentSnapshot?

        // Fetch user's posts
        dbTools.getTimelinePosts(userId, this.lastVisiblePost) { userPosts, newLastVisiblePost ->
            Log.d(tag, "Fetched user posts: ${userPosts.size}")
            newPosts.addAll(userPosts)
            combinedLastVisiblePost = newLastVisiblePost

            // Fetch friends' posts
            dbTools.getCurrentUserFollowing(userId) { following ->
                val followingCount = following.size
                var fetchedFriendPostsCount = 0

                following.forEach { friend ->
                    dbTools.getTimelinePosts(
                        friend.followedUserId,
                        this.lastVisiblePost
                    ) { friendPosts, _ ->
                        Log.d(tag, "Fetched friend posts: ${friendPosts.size}")
                        newPosts.addAll(friendPosts)
                        fetchedFriendPostsCount++

                        // If all friends' posts have been fetched
                        if (fetchedFriendPostsCount == followingCount) {
                            // Sort combined posts by date
                            newPosts.sortByDescending { it.date }

                            // Check if newPosts differ from currentPosts
                            if (newPosts != currentPosts) {
                                if (isPagination) {
                                    newPosts.forEach { addPost(it) }
                                } else {
                                    updatePosts(newPosts)
                                }
                            }
                            this.lastVisiblePost = combinedLastVisiblePost
                        }
                    }
                }

                // If no friends to fetch, update immediately
                if (followingCount == 0) {
                    newPosts.sortByDescending { it.date }
                    if (newPosts != currentPosts) {
                        if (isPagination) {
                            newPosts.forEach { addPost(it) }
                        } else {
                            updatePosts(newPosts)
                        }
                    }
                    this.lastVisiblePost = combinedLastVisiblePost
                }
            }
        }
        _isLoading.value = false

        // Attach timeline listener for real-time updates
        dbTools.attachTimelineListener(userId) { realTimeNewPosts ->
            realTimeNewPosts.forEach { addPost(it) }
        }
    }
}
