package com.example.fitconnect.tools

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R

class SearchUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val profilePictureImageView: ImageView = itemView.findViewById(R.id.searchProfileImageView)
    val userNameTextView: TextView = itemView.findViewById(R.id.searchUserNameTextView)
}