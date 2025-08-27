package com.example.fitconnect.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.activity.CreatePostActivity
import com.example.fitconnect.activity.ProfileSettingsActivity
import com.example.fitconnect.activity.ViewFollowingActivity
import com.example.fitconnect.activity.ViewUserProfileActivity
import com.example.fitconnect.adapter.PostAdapter
import com.example.fitconnect.databinding.FragmentTimelineBinding
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.ui.CurrentUserFeedViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso


private const val TAG = "CurrentUserFeedFragment"

/**
 * The specific fragment that displays the user's timeline (feed)
 *
 */
class CurrentUserFeedFragment(
    private val viewModel: CurrentUserFeedViewModel
) : Fragment(), View.OnClickListener {
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbTools: DBTools
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setBindings(user: FirebaseUser) {
        Log.d(TAG, "setBindings() called")
        // bindings for
        binding.timelineToolbar.inflateMenu(R.menu.test_friends_list)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = postAdapter
        binding.createPostButton.setOnClickListener(this)

        viewModel.timelinePosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.updatePosts(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadUserFeed(user.uid)
        }

        // Adding an on scroll listener for pagination
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    // load more posts when reaching end
                    viewModel.loadUserFeed(user.uid, isPagination = true)
                }
            }
        })

        // setup timeline toolbar
        binding.timelineToolbar.setOnMenuItemClickListener {
            val activity = requireActivity()
            when (it.itemId) {
                R.id.settings_button -> {
                    startActivity(Intent(activity, ProfileSettingsActivity::class.java))
                    true
                }

                R.id.friends_list -> {
                    startActivity(Intent(activity, ViewFollowingActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        dbTools = DBTools()

        postAdapter =
            PostAdapter(mutableListOf(), dbTools, isCurrentUser = true, viewModel) { postId ->
                openCommentFragment(postId)
            }
        // user shouldn't need to re-authenticate, but need uid
        val user = Firebase.auth.currentUser ?: return

        setBindings(user)
        viewModel.loadUserFeed(user.uid, isPagination = false)
    }

    private fun openCommentFragment(postId: String) {
        Log.d(TAG, "openCommentFragment() called")
        val commentFragment = CommentFragment().apply {
            arguments = Bundle().apply {
                putString("POST_ID", postId)
            }
        }
        parentFragmentManager.beginTransaction()
            .add(R.id.fragment_container, commentFragment, commentFragment::class.java.simpleName)
            .addToBackStack(commentFragment::class.java.simpleName)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.loadUserFeed(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView() called")
    }

    override fun onClick(v: View) {
        val activity = requireActivity()
        Log.d(TAG, "onClick() called")
        when (v.id) {
            binding.createPostButton.id -> {
                startActivity(Intent(activity, CreatePostActivity::class.java))
            }
        }
    }

}