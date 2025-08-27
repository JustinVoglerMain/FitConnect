package com.example.fitconnect.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.tools.SearchUserViewHolder
import com.squareup.picasso.Picasso


class SearchUsersAdapter(
    private val userList: MutableList<UserModel>,
    private val onUserClickListener: (UserModel) -> Unit
) : RecyclerView.Adapter<SearchUserViewHolder>() {
    private val tag = "SearchUsersAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_user_search, parent, false)
        return SearchUserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        Log.d(tag, "onBindViewHolder() called")
        val user = userList[position]
        val defaultImage = R.drawable.ic_default_profile_picture
        Picasso.get().load(user.profilePictureUrl)
            .placeholder(defaultImage)
            .placeholder(defaultImage)
            .error(defaultImage)
            .into(holder.profilePictureImageView)
        holder.userNameTextView.text = user.name
        holder.itemView.setOnClickListener {
            onUserClickListener(user)
        }
    }

    /**
     * Updates the current user list being display with the new list of users
     *
     * @param newUserList - the new list of users
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newUserList: List<UserModel>) {
        Log.d(tag, "updateList() called")
        userList.clear()
        userList.addAll(newUserList)
        notifyDataSetChanged()
    }
}