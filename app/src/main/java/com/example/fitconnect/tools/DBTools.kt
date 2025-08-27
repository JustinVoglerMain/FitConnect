package com.example.fitconnect.tools

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fitconnect.model.CommentModel
import com.example.fitconnect.model.FollowingModel
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.model.WorkoutModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
//import org.w3c.dom.Comment
import java.util.Date

private const val TAG = "DBTools"
private const val USER_COLLECTION = "users"
private const val TIMELINE_POSTS_COLLECTION = "timeline_posts"
private const val TIMELINE_COLLECTION = "timeline"
private const val FOLLOWER_COLLECTION = "followers"
private const val FOLLOWING_COLLECTION = "following"

/**
 * Handles CRUD actions and other misc requests/transactions/batches for the database
 *
 */
class DBTools {
    private val db = Firebase.firestore
    private val imageTools = ImageTools()


    // Function to add a new workout
    fun addWorkout(workout: WorkoutModel, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val newWorkoutRef = db.collection("workouts").document()
        workout.id = newWorkoutRef.id
        newWorkoutRef.set(workout)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Adds the given user to the firestore database
     *
     * @param user the user to add
     * @param callback returns true if successful, false otherwise
     */
    fun addUser(user: UserModel, callback: (Boolean) -> Unit) {
        Log.d(TAG, "addUser() called")
        val usersRef = db.collection(USER_COLLECTION).document(user.id)
        val xpDataRef = usersRef.collection("xpData").document("xp")
        val xpData = hashMapOf("xp" to 0)

        db.runBatch { batch ->
            batch.set(usersRef, user)
            batch.set(xpDataRef, xpData) // Add XP data
            val timelineRef = usersRef.collection(TIMELINE_COLLECTION).document("init")
            val followerRef = usersRef.collection(FOLLOWER_COLLECTION).document("init")
            val followingRef = usersRef.collection(FOLLOWING_COLLECTION).document("init")
            batch.set(timelineRef, emptyMap<String, Any>())
            batch.set(followerRef, emptyMap<String, Any>())
            batch.set(followingRef, emptyMap<String, Any>())
        }.addOnSuccessListener {
            Log.d(TAG, "Successfully added user to database and created timeline reference")
            callback(true)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error adding user to Firestore database", e)
            callback(false)
        }
    }



    /**
     * Deletes the user's document information related to the given uid
     *
     * @param uid the unique id of the user to be deleted
     * @param callback returns true if successful, false otherwise
     */
    fun deleteUser(uid: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "deleteUser() called")
        db.collection(USER_COLLECTION).document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Document successfully deleted!")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
                callback(false)
            }
    }

    /**
     * Fetches a user document from the database and converts it to a
     * UserModel object
     *
     * @param uid the unique id of the user
     * @param callback: (UserModel?) -> - A function that will utilize the result of this user
     */
    fun getUser(uid: String, callback: (UserModel?) -> Unit) {
        Log.d(TAG, "getUser() called")
        val userDocRef = db.collection(USER_COLLECTION).document(uid)

        Log.d(TAG, "Attempting to get document for uid: $uid")

        userDocRef.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "getUser(): onSuccessListener triggered")
                if (document != null) {
                    Log.d(TAG, "Document data received: ${document.data}")
                    val user = document.toObject<UserModel>()
                    if (user != null) {
                        // Fetch following, follower, and post counts
                        fetchUserCounts(userDocRef, user) { updatedUser ->
                            callback(updatedUser)
                        }
                    } else {
                        Log.d(TAG, "Document exists but user is null")
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "No such document exists")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "get failed with", e)
                callback(null)
            }
    }


    /**
     * Fetches the counts related to the user's profile statistics
     *
     * @param userDocRef the user document that represents the user
     * @param user the user being viewed
     * @param callback A new usermodel with counts updated
     */
    private fun fetchUserCounts(
        userDocRef: DocumentReference,
        user: UserModel,
        callback: (UserModel?) -> Unit
    ) {
        Log.d(TAG, "fetchUserCounts() called")
        val followingCollection = userDocRef.collection(FOLLOWING_COLLECTION)
        val followersCollection = userDocRef.collection(FOLLOWER_COLLECTION)
        val postsCollection = userDocRef.collection(TIMELINE_COLLECTION)

        followingCollection.get()
            .addOnSuccessListener { followingSnapshot ->
                user.followingCount = followingSnapshot.size() - 1

                followersCollection.get()
                    .addOnSuccessListener { followersSnapshot ->
                        user.followerCount = followersSnapshot.size() - 1

                        postsCollection.get()
                            .addOnSuccessListener { postsSnapshot ->
                                user.postCount = postsSnapshot.size() - 1
                                callback(user)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Failed to get post count", e)
                                callback(null)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to get follower count", e)
                        callback(null)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to get following count", e)
                callback(null)
            }
    }


    /**
     * Updates a string field for the user
     *
     * @param uid the unique id of the user
     * @param field the field to update
     * @param updates the updates for the user
     * @param callback true if the update worked, false otherwise
     */
    fun updateUserFieldString(
        uid: String,
        field: String,
        updates: String,
        callback: (Boolean) -> Unit
    ) {
        val userDocumentRef = db.collection(USER_COLLECTION).document(uid)
        userDocumentRef
            .update(field, updates)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot for $uid successfully updated.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                callback(false)
            }
    }

    /**
     * Add a post to the TIMELINE_POSTS_COLLECTION on the database
     *
     * @param post the specific post to add
     * @param callback the callback function returning true if successful, false otherwise
     */
    fun addPost(post: PostModel, callback: (Boolean) -> Unit) {
        Log.d(TAG, "addPost() called")
        db.collection(TIMELINE_POSTS_COLLECTION).document(post.postId).set(post)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully added post")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding post:", e)
                callback(false)
            }
    }

    /**
     * Removes the image from the post from imgur
     *
     * @param post the post with the image
     * @param callback true if removal was successful, false otherwise
     */
    private fun removePostImage(post: PostModel, callback: (Boolean) -> Unit) {
        imageTools.deleteImageFromImgur(post.deleteHash) { success ->
            if (success) {
                Log.d(TAG, "Successfully removed image")
                callback(true)
            } else {
                Log.e(TAG, "Unable to remove image: ${post.imgURL}")
                callback(false)
            }
        }
    }

    /**
     * Removes the given post from the database
     *
     * @param post the post to remove
     * @param callback true if the removal was successful, false otherwise
     */
    private fun removePostFun(post: PostModel, callback: (Boolean) -> Unit) {
        db.collection(USER_COLLECTION).document(post.posterUID)
            .collection(TIMELINE_COLLECTION)
            .document(post.postId)
            .delete()
            .addOnSuccessListener {
                db.collection(TIMELINE_POSTS_COLLECTION).document(post.postId).delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully removed post")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error removing post with id ${post.postId}", e)
                        callback(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error removing post from user's timeline ref collection", e)
                callback(false)
            }


    }

    /**
     * Remove's the user's post from both their timeline and the collection of timeline posts
     *
     * @param post the post to be removed
     * @param callback the callback handling result of the function (true if removed, false otherwise)
     */
    fun removePost(post: PostModel, callback: (Boolean) -> Unit) {
        Log.d(TAG, "removePost() called")

        if (post.imgURL.isNotBlank() && post.deleteHash.isNotBlank()) {
            removePostImage(post) { imageDeleteResult ->
                if (imageDeleteResult) {
                    Log.d(TAG, "Successfully removed image")
                    removePostFun(post) { removal ->
                        if (removal) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    }
                }
            }
        } else {
            removePostFun(post) { removal ->
                if (removal) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
        }
    }

    /**
     * Adds a specific post to the user's subcollection timeline (call AFTER adding a post to timeline)
     *
     * @param userId the id of the user who's having the post added to their timeline
     * @param post the post to be added to the user's timeline
     * @param callback  the callback handling result of the function (true if removed, false otherwise)
     */
    fun addPostToUserTimeline(userId: String, post: PostModel, callback: (Boolean) -> Unit) {
        val timelineRef = db.collection(USER_COLLECTION)
            .document(userId)
            .collection(TIMELINE_COLLECTION)
            .document(post.postId)

        timelineRef.set(
            mapOf(
                "postId" to post.postId,
                "date" to post.date
            )
        )
            .addOnSuccessListener {
                Log.d(TAG, "Post reference added to user's timeline")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding post reference to timeline", e)
                callback(false)
            }
    }

    /**
     * Adds the followed user to the current user's following list and adds the current user
     * to the follower list of the followed user
     *
     * @param userToFollowModel the mdodel representing following users
     * @param callback true if the transaction was successful , false otherwise
     */
    fun followUser(userToFollowModel: FollowingModel, callback: (Boolean) -> Unit) {
        val followingRef = db.collection(USER_COLLECTION)
            .document(userToFollowModel.currentUserId)
            .collection(FOLLOWING_COLLECTION)
            .document(userToFollowModel.followedUserId)

        val followerRef = db.collection(USER_COLLECTION)
            .document(userToFollowModel.followedUserId)
            .collection(FOLLOWER_COLLECTION)
            .document(userToFollowModel.currentUserId)

        db.runTransaction { transaction ->
            // Fetch current data first
            val followedUserDocRef =
                db.collection(USER_COLLECTION).document(userToFollowModel.followedUserId)
            val currentUserDocRef =
                db.collection(USER_COLLECTION).document(userToFollowModel.currentUserId)
            val followedUserSnapshot = transaction.get(followedUserDocRef)
            val currentUserSnapshot = transaction.get(currentUserDocRef)

            // Perform all writes after the reads
            transaction.set(
                followingRef, mapOf(
                    "currentUserId" to userToFollowModel.currentUserId,
                    "followedUserId" to userToFollowModel.followedUserId,
                    "followedUserName" to userToFollowModel.followedUserName,
                    "dateFollowed" to userToFollowModel.dateFollowed
                )
            )
            val followerName = Firebase.auth.currentUser?.displayName ?: "User"

            transaction.set(
                followerRef, mapOf(
                    "followedUserId" to userToFollowModel.followedUserId,
                    "followerId" to userToFollowModel.currentUserId,
                    "followerName" to followerName,
                    "dateFollowed" to userToFollowModel.dateFollowed
                )
            )

            val newFollowerCount = (followedUserSnapshot.getLong("followerCount") ?: 0L) + 1
            transaction.update(followedUserDocRef, "followerCount", newFollowerCount)

            val newFollowingCount = (currentUserSnapshot.getLong("followingCount") ?: 0L) + 1
            transaction.update(currentUserDocRef, "followingCount", newFollowingCount)
        }.addOnSuccessListener {
            Log.d(TAG, "Successfully followed user")
            callback(true)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed to follow user", e)
            callback(false)
        }
    }

    fun unfollowUser(currentUserId: String, userToUnfollowId: String, callback: (Boolean) -> Unit) {
        val followingRef = db.collection(USER_COLLECTION)
            .document(currentUserId)
            .collection(FOLLOWING_COLLECTION)
            .document(userToUnfollowId)

        val followerRef = db.collection(USER_COLLECTION)
            .document(userToUnfollowId)
            .collection(FOLLOWER_COLLECTION)
            .document(currentUserId)

        db.runTransaction { transaction ->
            transaction.delete(followingRef)
            transaction.delete(followerRef)
        }.addOnSuccessListener {
            Log.d(TAG, "Successfully removed users from following/Follower list")
            callback(true)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Transaction failed for unfollowing user: ${e.message}")
            callback(false)
        }
    }

    fun checkIfViewerFollowsUser(
        currentUserId: String,
        userToUnfollowId: String,
        callback: (Boolean) -> Unit
    ) {
        val followingRef = db.collection(USER_COLLECTION)
            .document(currentUserId)
            .collection(FOLLOWING_COLLECTION)
            .document(userToUnfollowId)

        followingRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                callback(true)
            } else {
                callback(false)
            }
        }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking if user follows this user", e)
                callback(false)
            }
    }


    /**
     * Gets a list of (15) friends to be displayed when looking at the friends list (can add more)
     *
     * @param userId the id of the user looking at their friends list
     * @param lastFriendShown the snapshot of the last friend in the list
     * @param callback The list of friends after the previous list of friends including the last friend shown in the new list
     */
    fun getFollowingList(
        userId: String,
        lastFriendShown: DocumentSnapshot? = null,
        searchQuery: String = "",
        callback: (List<FollowingModel>, DocumentSnapshot?) -> Unit
    ) {
        Log.d(TAG, "getFriendsList() called")
        val friendsListRef = db.collection(USER_COLLECTION)
            .document(userId)
            .collection(FOLLOWER_COLLECTION)
            .limit(15)

        // check if the friends list has been updated
        if (lastFriendShown != null) {
            friendsListRef.startAfter(lastFriendShown)
        }

        // filter by search query if included
        val query = friendsListRef.orderBy("friendName", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("friendName", searchQuery)

        query.get()
            .addOnSuccessListener { friends ->
                val friendModels = friends.documents.mapNotNull { document ->
                    val friend = document.toObject<FollowingModel>()
                    friend?.document = document
                    friend // returns friend model object
                }

                if (friendModels.isEmpty()) {
                    callback(emptyList(), null)
                    return@addOnSuccessListener
                }

                val friendIds = friendModels.map { it.followedUserId }
                db.collection(USER_COLLECTION)
                    .whereIn("userId", friendIds)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        // Combining friends data with their profile information
                        val combined = friendModels.map { friendModel ->
                            val userDoc =
                                userDocs.documents.find { it.id == friendModel.followedUserId }
                            val user = userDoc?.toObject<UserModel>()
                            friendModel.copy(
                                followedUserName = user?.name ?: "",
                                followedUserProfileUrl = user?.profilePictureUrl ?: ""
                            )
                        }
                        val lastFriend = friends.documents.lastOrNull()
                        callback(combined, lastFriend)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error getting user profiles", e)
                        callback(emptyList(), null)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting friends from firestore database", e)
                callback(emptyList(), null)
            }
    }

    /**
     * Gets a list of users (15) from the given search query
     *
     * @param lastUserShown - the document snapshot of the last user shown (optional, default = null)
     * @param searchQuery - the search query to be used if desired (optional, default = "")
     * @param callback a list of users and the document snapshot of the last user in the list or null
     */
    fun getUsersFromSearchQuery(
        lastUserShown: DocumentSnapshot? = null,
        searchQuery: String = "",
        callback: (List<UserModel>, DocumentSnapshot?) -> Unit
    ) {
        Log.d(TAG, "getUsersFromSearchQuery() called")
        val userListRef = db.collection(USER_COLLECTION).limit(15)
        if (lastUserShown != null) {
            userListRef.startAfter(lastUserShown)
        }
        val endRange = searchQuery + "\uf8ff"

        val query = userListRef.orderBy("name", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("name", searchQuery)
            .whereLessThan("name", endRange)


        query.get()
            .addOnSuccessListener { users ->
                Log.d(TAG, "successfully retrieved list of users")
                val userModels = users.documents.mapNotNull { doc ->
                    val userFromSearch = doc.toObject<UserModel>()
                    userFromSearch
                }
                if (userModels.isEmpty()) {
                    callback(emptyList(), null)
                    return@addOnSuccessListener
                }
                val lastDocument = users.documents.lastOrNull()
                callback(userModels, lastDocument)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user information from database", e)
                callback(emptyList(), null)
            }
    }

    /**
     * Gets the 10 most recent timeline posts for a user
     *
     * @param userId the user to get timeline posts from
     * @param lastVisiblePost the last visible post (for pagination)
     * @param callback The callback containing a list of timeline posts
     */
    fun getTimelinePosts(
        userId: String,
        lastVisiblePost: DocumentSnapshot? = null,
        callback: (List<PostModel>, DocumentSnapshot?) -> Unit
    ) {
        Log.d(TAG, "getTimelinePosts() called")
        val timelineRef = db.collection(USER_COLLECTION)
            .document(userId)
            .collection(TIMELINE_COLLECTION)
            .limit(10)

        if (lastVisiblePost != null) {
            timelineRef.startAfter(lastVisiblePost)
        }

        timelineRef.orderBy("date", Query.Direction.DESCENDING) // Order by date for pagination
            .get()
            .addOnSuccessListener { documents ->
                val postIds = documents.documents.mapNotNull { it.id }
                if (postIds.isEmpty()) {
                    Log.d(
                        TAG,
                        "Empty list returned. If this is a mistake please verify with the database"
                    )
                    callback(emptyList(), null)
                    return@addOnSuccessListener
                }

                // Fetch posts by ID
                db.collection(TIMELINE_POSTS_COLLECTION)
                    .whereIn("postId", postIds)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        val posts = result.documents.mapNotNull { it.toObject<PostModel>() }
                        val newLastVisiblePost = result.documents.lastOrNull()
                        callback(posts, newLastVisiblePost)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error getting user timeline posts", e)
                        callback(emptyList(), null)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting timeline references", e)
                callback(emptyList(), null)
            }
    }


    /**
     * Gets the list of user the current user is following
     *
     * @param userId the current user's id
     * @param callback the specific functions for what to do with user information
     */
    fun getCurrentUserFollowing(userId: String, callback: (List<FollowingModel>) -> Unit) {
        val followingRef = db.collection(USER_COLLECTION).document(userId).collection(
            FOLLOWING_COLLECTION
        )
        followingRef.get()
            .addOnSuccessListener { documents ->
                val followingList = documents.documents.filter { it.id != "init" }
                    .mapNotNull { it.toObject<FollowingModel>() }
                callback(followingList)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting current user's following list", e)
                callback(emptyList())
            }
    }


    /**
     * Attaches a timeline listener to a specific user's timeline to dynamically get any
     * possible updates for the user's timeline
     *
     * @param userID the id of the specific user that needs the listener
     * @param callback the callback function handling the the new list of posts
     */
    fun attachTimelineListener(userID: String, callback: (List<PostModel>) -> Unit) {
        val timelineRef = db.collection(USER_COLLECTION).document(userID).collection(
            TIMELINE_COLLECTION
        )
        timelineRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Failed to attach listener")
                return@addSnapshotListener
            }

            val postIds = snapshot?.documents?.mapNotNull { it.id } ?: return@addSnapshotListener
            if (postIds.isEmpty()) {
                callback(emptyList())
                return@addSnapshotListener
            }
            db.collectionGroup(TIMELINE_POSTS_COLLECTION)
                .whereIn("postIds", postIds)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val timelinePosts = result.documents.mapNotNull { it.toObject<PostModel>() }
                    callback(timelinePosts)
                }
                .addOnFailureListener { er ->
                    Log.w(TAG, "Error getting posts", er)
                    callback(emptyList())
                }
        }
    }

    //edit posts
    fun updatePostContent(postId: String, newContent: String, callback: (Boolean) -> Unit) {
        val postRef = db.collection("timeline_posts").document(postId)

        postRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                postRef.update("content", newContent)
                    .addOnSuccessListener {
                        Log.d("DBTools", "updatePostContent: Post content updated successfully")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("DBTools", "updatePostContent: Failed to update post content", e)
                        callback(false)
                    }
            } else {
                Log.e("DBTools", "updatePostContent: No document with postId $postId")
                callback(false)
            }
        }.addOnFailureListener { e ->
            Log.e("DBTools", "updatePostContent: Failed to retrieve post document", e)
            callback(false)
        }
    }


    //like posts

    fun toggleLike(postId: String, userId: String, onComplete: (Boolean) -> Unit) {
        val likeRef = Firebase.firestore.collection("posts").document(postId).collection("likes")
            .document(userId)
        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(likeRef)
            if (snapshot.exists()) {
                transaction.delete(likeRef)
                false
            } else {
                transaction.set(likeRef, mapOf("liked" to true))
                true
            }
        }.addOnSuccessListener { liked ->
            onComplete(liked)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun getLikesCount(postId: String, onComplete: (Int) -> Unit) {
        Firebase.firestore.collection("posts").document(postId).collection("likes")
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.size())
            }
    }

    fun getCommentsCount(postId: String, onComplete: (Int) -> Unit) {
        Firebase.firestore.collection("posts").document(postId).collection("comments")
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.size())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching comments count: ", exception)
                onComplete(0)  // Return 0 in case of an error
            }
    }

    fun isUserLiked(postId: String, userId: String, onComplete: (Boolean) -> Unit) {
        Firebase.firestore.collection("posts").document(postId).collection("likes")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                onComplete(document.exists())
            }
    }

    fun addComment(
        postId: String,
        userId: String,
        commentText: String,
        onComplete: (Boolean) -> Unit
    ) {
        val commentRef = db.collection("posts").document(postId).collection("comments").document()
        val commentData = mapOf(
            "text" to commentText,
            "userId" to userId,
            "timestamp" to FieldValue.serverTimestamp()
        )
        commentRef.set(commentData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    fun getComments(postId: String, callback: (List<CommentModel>) -> Unit) {
        val commentsRef = Firebase.firestore.collection("posts")
            .document(postId)
            .collection("comments")

        commentsRef.get().addOnSuccessListener { querySnapshot ->
            val comments = mutableListOf<CommentModel>()
            for (document in querySnapshot.documents) {
                val userId = document.getString("userId") ?: ""
                val text = document.getString("text") ?: ""
                val userName = document.getString("userName") ?: ""
                val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()


                // Create CommentModel and add to list
                //comments.add(CommentModel(userId, text, timestamp))
                comments.add(
                    CommentModel(
                        document.id,
                        userName,
                        userId,
                        text,
                        timestamp.toString()
                    )
                )
            }
            callback(comments)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching comments: ", exception)
            callback(emptyList())  // Return an empty list on error
        }
    }

    fun deleteComment(postId: String, commentId: String, callback: (Boolean) -> Unit) {
        val commentRef = Firebase.firestore.collection("posts").document(postId)
            .collection("comments").document(commentId)
        Log.d(TAG, "Attempting to delete document at path: /posts/$postId/comments/$commentId")
        commentRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Comment successfully deleted.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting comment", e)
                callback(false)
            }
    }

    fun updateComment(
        postId: String,
        commentId: String,
        updatedText: String,
        callback: (Boolean) -> Unit
    ) {
        val commentRef = Firebase.firestore.collection("posts").document(postId)
            .collection("comments").document(commentId)

        commentRef.update("text", updatedText)  // Make sure "text" is the correct field name
            .addOnSuccessListener {
                Log.d(TAG, "Comment successfully updated.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating comment", e)
                callback(false)
            }
    }

    /** ~~~~~~~~~~~~~~~~~~ IMAGE FUNCTIONS ~~~~~~~~~~~~~~~~~ */
    /**
     * Upload and store a specific image into the imgur database
     *
     * @param context the specific context for uploading the image
     * @param uri the uri of the image to be uploaded
     * @param callback the image url and image id on successful upload, null otherwise
     */
    fun uploadImage(context: Context, uri: Uri, callback: (String?, String?) -> Unit) {
        imageTools.uploadImageToImgur(context, uri) { imgUrl, deleteHash ->
            if (imgUrl != null) {
                Log.d(TAG, "Image Uploaded Successfully")
                callback(imgUrl, deleteHash)
            } else {
                Log.d(TAG, "Image failed to upload")
                callback(null, null)
            }
        }
    }


}