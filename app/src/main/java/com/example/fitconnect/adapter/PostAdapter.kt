package com.example.fitconnect.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.CommentModel
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.PostViewHolder
import com.example.fitconnect.ui.CurrentUserFeedViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

const val TAG = "PostAdapter"

/**
 * An adapter class used for dynamic updates when creating a profile post
 *
 * @property postModel - the model of a post
 * @property dbTools - the database tools
 */
class PostAdapter(
    private val postModel: MutableList<PostModel>,
    private val dbTools: DBTools,
    private val isCurrentUser: Boolean = false,
    private val currentUserFeedViewModel: CurrentUserFeedViewModel? = null,
    private val onCommentClick: (String) -> Unit
) :
    RecyclerView.Adapter<PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        Log.d(TAG, "OnCreateViewHolder() called")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_user_post, parent, false)
        return PostViewHolder(view)
    }

    /**
     * Sets the listener for creating a new post if the person viewing the feed is the current user
     *
     * @param holder the view holder for posts
     * @param post the post being created
     * @param position the position for this post
     */
    private fun setPostOnClickListener(holder: PostViewHolder, post: PostModel, position: Int) {
        // Menu button click listener for settings
        Log.d(TAG, "Setting up postSettingsButton")
        holder.postSettingsButton.setOnClickListener {
            Log.d("PostAdapter", "postSettingsButton clicked")
            val popupMenu = PopupMenu(holder.postSettingsButton.context, holder.postSettingsButton)
            popupMenu.inflate(R.menu.post_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        // TODO: Add functionality to edit post
                        true
                    }

                    R.id.action_delete -> {
                        showDeleteConfirmationDialog(holder.postSettingsButton.context) {
                            dbTools.removePost(post) { success ->
                                if (success) {
                                    currentUserFeedViewModel?.removePost(post)
                                    postModel.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, postModel.size)
                                    Snackbar.make(
                                        holder.itemView, "Post deletion successful",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder() called")
        val post = postModel[position]

        // For when the user is looking at their own feed
        if (isCurrentUser && post.isCurrentUser) {
            setPostOnClickListener(holder, post, position)
        }

        holder.commentButton.setOnClickListener {
            onCommentClick(post.postId)  // Pass postId to open CommentFragment
        }

        // Display post content and image
        holder.posterDisplayName.text = post.posterName
        holder.posterContent.text = post.content
        if (post.imgURL.isNotBlank()) {
            holder.postImage.visibility = View.VISIBLE
            Picasso.get().load(post.imgURL).into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        dbTools.isUserLiked(post.postId, post.posterUID) { liked ->
            holder.likeButton.alpha = if (liked) 1.0f else 0.5f
        }

        // Like button click listener
        holder.likeButton.setOnClickListener {
            dbTools.toggleLike(post.postId, post.posterUID) { liked ->
                holder.likeButton.alpha = if (liked) 1.0f else 0.5f
                dbTools.getLikesCount(post.postId) { count ->
                    val countString = "$count likes"
                    holder.likesCountTextView.text = countString
                }
            }
        }

        dbTools.getCommentsCount(post.postId) { count ->
            val countString2 = "$count likes"
            holder.commentsCountTextView.text = countString2
        }
        // Menu button click listener for settings
        holder.postSettingsButton.setOnClickListener {
            val popupMenu = PopupMenu(holder.postSettingsButton.context, holder.postSettingsButton)
            popupMenu.inflate(R.menu.post_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        showEditPostDialog(
                            holder.postSettingsButton.context,
                            post.content
                        ) { newContent ->
                            if (newContent.isNotBlank()) {
                                post.content = newContent

                                dbTools.updatePostContent(post.postId, newContent) { success ->
                                    if (success) {
                                        Snackbar.make(
                                            holder.itemView,
                                            "Post updated successfully",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                        postModel[position] = post
                                        notifyItemChanged(position) // Update item in RecyclerView
                                    } else {
                                        Snackbar.make(
                                            holder.itemView,
                                            "Failed to update post",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                        true
                    }

                    R.id.action_delete -> {
                        showDeleteConfirmationDialog(holder.postSettingsButton.context) {
                            dbTools.removePost(post) { success ->
                                if (success) {
                                    postModel.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, postModel.size)
                                    Snackbar.make(
                                        holder.itemView,
                                        "Deletion successful",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
        //comments
        holder.commentButton.setOnClickListener {
            onCommentClick(post.postId)
        }

        // Load comments dynamically

        loadComments(post.postId, holder)
    }

    // Method to load comments dynamically into the commentsContainer
    private fun loadComments(postId: String, holder: PostViewHolder) {
        dbTools.getComments(postId) { comments: List<CommentModel> ->
            // Clear existing comments
            holder.commentsContainer.removeAllViews()

            // Update comments count
            val commentSize = "${comments.size} comments"

            holder.commentsCountTextView.text = commentSize

            // Add each comment as a TextView in the commentsContainer
            for (comment in comments) {
                val commentTextView = TextView(holder.itemView.context)
                val commentText = "${comment.userId}: ${comment.text}"
                commentTextView.text = commentText
                holder.commentsContainer.addView(commentTextView)
            }
        }
    }

    private fun showEditPostDialog(
        context: Context,
        currentContent: String,
        onConfirm: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Post")

        val input = EditText(context)
        input.setText(currentContent)
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newContent = input.text.toString()
            onConfirm(newContent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun getItemCount() = postModel.size

    /**
     * Shows a dialogue box confirmation message when deleting a post
     *
     * @param context - the respective context calling this
     * @param onConfirm - the actions to be taken once a user presses Confirm
     */
    private fun showDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Post")
        builder.setMessage("Are you sure you want to permanently delete this post?")
        builder.setPositiveButton("Yes") { _, _ -> onConfirm() }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    /**
     * Updates the previous posts with a new posts
     *
     * @param newPosts - the new posts to replace this
     */
    fun updatePosts(newPosts: List<PostModel>) {
        val oldSize = postModel.size
        val newSize = newPosts.size
        postModel.clear()
        postModel.addAll(newPosts)
        // notify the viewModel of changes
        notifyItemRangeRemoved(0, oldSize)
        notifyItemRangeInserted(0, newSize)
    }

    /**
     * Adds new posts to this
     *
     * @param newPosts the list of new posts to be added to this
     */
    fun addPosts(newPosts: List<PostModel>) {
        val startPosition = postModel.size
        postModel.addAll(newPosts)
        notifyItemRangeInserted(0, startPosition)
    }
}