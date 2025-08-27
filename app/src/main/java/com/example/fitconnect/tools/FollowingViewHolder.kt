package com.example.fitconnect.tools

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R

class FollowingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    val friendNameTextView: TextView = itemView.findViewById(R.id.friendNameTextView)
    val friendStatusTextView: TextView = itemView.findViewById(R.id.friendStatusTextView)
    val contextMenuImageView: ImageView = itemView.findViewById(R.id.contextMenuImageView)
}