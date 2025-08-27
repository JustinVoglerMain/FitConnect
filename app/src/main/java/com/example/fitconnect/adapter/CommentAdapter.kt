package com.example.fitconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.CommentModel

class CommentAdapter(
    private var comments: MutableList<CommentModel>,
    private val currentUserId: String,
//    private val onEditComment: (CommentModel, String) -> Unit,
//    private val onDeleteComment: (CommentModel) -> Unit

    private val onEditComment: (String, String) -> Unit,
    private val onDeleteComment: (String) -> Unit

) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {


    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
        val commentEditText: EditText = itemView.findViewById(R.id.commentEditText)
        val commentDateTextView: TextView = itemView.findViewById(R.id.commentDateTextView)
        val editCommentButton: ImageButton = itemView.findViewById(R.id.editCommentButton)
        val deleteCommentButton: ImageButton = itemView.findViewById(R.id.deleteCommentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val documentId = comment.documentId
        var isEditing = false // Local variable to track edit mode

        // Display comment text and timestamp
        holder.commentTextView.text = comment.text // Display the comment text
        holder.commentTextView.append("\n${comment.userName}") // Optionally show the username too
        holder.commentDateTextView.text = comment.timestamp

        // Show edit and delete buttons only if the current user is the author
        if (comment.userId == currentUserId) {
            holder.editCommentButton.visibility = View.VISIBLE
            holder.deleteCommentButton.visibility = View.VISIBLE
        } else {
            holder.editCommentButton.visibility = View.GONE
            holder.deleteCommentButton.visibility = View.GONE
        }

        // Toggle between edit and save mode when the edit button is clicked
        holder.editCommentButton.setOnClickListener {
            if (isEditing) {
                // Save the comment when exiting edit mode
                val updatedText = holder.commentEditText.text.toString()
                if (updatedText.isNotBlank() && updatedText != comment.text) {
                    onEditComment(documentId, updatedText) // Call edit function
                    holder.commentTextView.text = updatedText
                    Toast.makeText(holder.itemView.context, "Comment saved", Toast.LENGTH_SHORT)
                        .show()
                }

                // Switch back to view mode
                holder.commentTextView.visibility = View.VISIBLE
                holder.commentEditText.visibility = View.GONE
                holder.editCommentButton.setImageResource(R.drawable.ic_edit) // Change icon back to edit
            } else {
                // Enable edit mode
                holder.commentEditText.setText(comment.text)
                holder.commentTextView.visibility = View.GONE
                holder.commentEditText.visibility = View.VISIBLE
                holder.editCommentButton.setImageResource(R.drawable.ic_edit) // Change icon to save
            }

            isEditing = !isEditing // Toggle edit mode
        }

        // Delete button listener
        holder.deleteCommentButton.setOnClickListener {
            onDeleteComment(documentId)
//            comments.removeAt(position)
//            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentModel>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}
