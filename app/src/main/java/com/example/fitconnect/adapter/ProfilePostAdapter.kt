// ProfilePostAdapter.kt
package com.example.fitconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.PostModel
import com.squareup.picasso.Picasso

class ProfilePostAdapter(private val posts: MutableList<PostModel>) :
    RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterNameTextView: TextView = itemView.findViewById(R.id.posterName)
        val postContentTextView: TextView = itemView.findViewById(R.id.postContent)
        val postImageView: ImageView = itemView.findViewById(R.id.postImage)
        val likesCountTextView: TextView = itemView.findViewById(R.id.likesCountTextView)
        val commentsCountTextView: TextView = itemView.findViewById(R.id.commentsCountTextView)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timeline_user_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.posterNameTextView.text = post.posterUID  // Ensure `PostModel` has a userName field
        holder.postContentTextView.text = post.content

        val likesCount = "${post.likesCount} likes"
        val commentsCount = "${post.commentsCount} comments"
        holder.likesCountTextView.text = likesCount
        holder.commentsCountTextView.text = commentsCount

        // Load post image if available
        if (post.imgURL.isNotEmpty()) {
            holder.postImageView.visibility = View.VISIBLE
            Picasso.get().load(post.imgURL).into(holder.postImageView)
        } else {
            holder.postImageView.visibility = View.GONE
        }

        holder.likeButton.setOnClickListener {
            // Implement like button functionality if needed
        }

        // Handle comment button
        holder.commentButton.setOnClickListener {
            // Implement comment button functionality if needed
        }


    }

    override fun getItemCount(): Int = posts.size

    // Add/update posts efficiently
    fun updatePosts(newPosts: List<PostModel>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}
