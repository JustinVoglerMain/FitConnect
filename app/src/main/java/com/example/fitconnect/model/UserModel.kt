package com.example.fitconnect.model

/**
 * Class to represent the user model for the database as it requires an empty constructor
 *
 */
data class UserModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var profilePictureUrl: String = DEFAULT_PROFILE_PICTURE_URL,
    var createdAt: Long = System.currentTimeMillis(),
    var bio: String = "",
    var followingCount: Int = 0,
    var followerCount: Int = 0,
    var postCount: Int = 0,
    var xp: Int = 0
) {
    companion object {
        const val DEFAULT_PROFILE_PICTURE_URL =
            "android.resource://com.example.fitconnect/drawable/ic_default_profile_picture"
    }
}

