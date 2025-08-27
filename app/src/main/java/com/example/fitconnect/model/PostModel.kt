package com.example.fitconnect.model

import java.util.Date

/**
 * This class models a post. It is not a data class as the firestore database requires the
 * models to have an empty constructor.
 *
 * @property postId the id of the post
 * @property posterUID the id of the poster
 * @property posterName the name of the poster
 * @property date the date of the post
 * @property content the content of the post
 * @property imgURL  an imgur url link used when deleting a post with an image
 * @property deleteHash the hash information required for deleting an image from imgur
 * @property isCurrentUser a boolean to track whether the post has been created by the current user
 */
data class PostModel(
    var postId: String = "",
    var posterUID: String = "",
    var posterName: String = "",
    var date: Date = Date(),
    var content: String = "",
    var imgURL: String = "",
    var deleteHash: String = "",
    var likesCount: String = "0",
    var commentsCount: String = "0",
    var isCurrentUser: Boolean = false
)
