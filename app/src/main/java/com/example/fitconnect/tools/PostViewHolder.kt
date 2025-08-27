package com.example.fitconnect.tools

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R

/**
 * Class to handle how posts are viewed in a user's timeline/feed
 *
 * @constructor
 *
 *
 * @param itemView the specific item view from the Recycler View
 */
class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val posterDisplayName: TextView = itemView.findViewById(R.id.posterName)
    val posterContent: TextView = itemView.findViewById(R.id.postContent)
    val postImage: ImageView = itemView.findViewById(R.id.postImage)
    val postSettingsButton: ImageButton = itemView.findViewById(R.id.postSettingsButton)
    val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
    val likesCountTextView: TextView = itemView.findViewById(R.id.likesCountTextView)
    val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
    val commentsCountTextView: TextView = itemView.findViewById(R.id.commentsCountTextView)
    val commentsContainer: LinearLayout = itemView.findViewById(R.id.commentsContainer)
}