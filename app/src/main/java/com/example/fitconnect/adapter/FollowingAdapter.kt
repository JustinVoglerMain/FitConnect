package com.example.fitconnect.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.FollowingModel
import com.example.fitconnect.tools.FollowingViewHolder
import com.google.firebase.firestore.DocumentSnapshot

class FollowingAdapter(
    private val friendsList: MutableList<FollowingModel>,
    private val onFriendClickListener: (FollowingModel, DocumentSnapshot?) -> Unit,
    private val onFriendDeleteClickListener: (FollowingModel, DocumentSnapshot?) -> Unit
) : RecyclerView.Adapter<FollowingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_friends, parent, false)
        return FollowingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val friend = friendsList[position]
        val friendDocument = friend.document
        holder.friendNameTextView.text = friend.followedUserName
        holder.friendStatusTextView.text = friend.followingStatus

        holder.contextMenuImageView.setOnClickListener{
            onFriendDeleteClickListener(friend, friendDocument)
        }

        holder.itemView.setOnClickListener {
            // for opening up the user profile
            onFriendClickListener(friend, friendDocument)
        }

    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFollowing(newFriendsList: List<FollowingModel>) {
        friendsList.clear()
        friendsList.addAll(newFriendsList)
        // We want this because we are reloading the entire friends list page
        notifyDataSetChanged()
    }

    fun addNewFriend(newFriend: FollowingModel) {
        friendsList.add(newFriend)
        notifyItemInserted(friendsList.size - 1)
    }
}