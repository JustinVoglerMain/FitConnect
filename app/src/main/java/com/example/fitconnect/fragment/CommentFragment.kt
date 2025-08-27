package com.example.fitconnect.fragment

//import androidx.lifecycle.Observer
//import com.example.fitconnect.tools.DBTools
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitconnect.R
import com.example.fitconnect.adapter.CommentAdapter
import com.example.fitconnect.databinding.FragmentCommentBinding
import com.example.fitconnect.ui.CommentViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


private const val COMMENTS_COLLECTION = "comments"


class CommentFragment : Fragment() {
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentAdapter: CommentAdapter

    //private lateinit var dbTools: DBTools
    private lateinit var postId: String  // Pass postId to this fragment for loading comments
    private lateinit var commentViewModel: CommentViewModel
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        postId = arguments?.getString("POST_ID") ?: ""

        commentViewModel = ViewModelProvider(this)[CommentViewModel::class.java]

        // Set up RecyclerView and Adapter
        val currentUserId = Firebase.auth.currentUser?.uid ?: ""
        setupRecyclerView(currentUserId)

        commentViewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentAdapter.updateComments(comments)
        }

        // Load initial comments
        commentViewModel.loadComments(postId)

        // Add comment when post button is clicked
        binding.postCommentButton.setOnClickListener {
            val userId = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
            //val userName = Firebase.auth.currentUser?.displayName ?: return@setOnClickListener
            val commentText = binding.commentEditText.text.toString()
            if (commentText.isNotBlank()) {
                commentViewModel.addComment(postId, userId, commentText)
                binding.commentEditText.text.clear()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentToolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        binding.commentToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView(currentUserId: String) {
        commentAdapter = CommentAdapter(
            comments = mutableListOf(),
            currentUserId = currentUserId,
            onEditComment = { commentId, updatedText ->
                updateComment(
                    postId,
                    commentId,
                    updatedText
                ) // Pass both postId and commentId for editing
            },
            onDeleteComment = { commentId ->
                deleteComment(postId, commentId) // Pass both postId and commentId for deletion
            }
        )
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
    }

    private fun updateComment(postId: String, commentId: String, updatedText: String) {
        commentViewModel.updateComment(postId, commentId, updatedText)
    }

    private fun deleteComment(documentId: String, commentId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                commentViewModel.deleteComment(
                    documentId,
                    commentId
                ) // Pass documentId for deletion
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

    }
}
