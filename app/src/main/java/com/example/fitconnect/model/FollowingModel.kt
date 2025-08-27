package com.example.fitconnect.model

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

/**
 * Models the list of users the current user is following
 *
 * currentUserId : This represents the ID of the current user
 *
 * friendId : This is the unique ID of the follower
 *
 * friendName : the followers name
 *
 * friendProfilePictureUrl : The URL of the friend's profile picture (optional).
 *
 * friendStatus : This represents the interaction status between friends (last interacted 3 day, months, etc)
 *
 * dateAdded : The date when the friendship was established.
 *
 * document : The snapshot of the friend's document to prevent having to read and get user information
 */
data class FollowingModel(
    var currentUserId: String = "",
    var followedUserId: String = "",
    var followedUserName: String = "",
    var followedUserProfileUrl: String? = null, // Optional
    var followingStatus: String = "",
    var dateFollowed: Date = Date(),
    var document: DocumentSnapshot? = null
)