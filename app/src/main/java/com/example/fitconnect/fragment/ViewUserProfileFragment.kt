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
import com.example.fitconnect.activity.ViewFollowingActivity
import com.example.fitconnect.adapter.PostAdapter
import com.example.fitconnect.databinding.FragmentViewUserProfileBinding
import com.example.fitconnect.model.FollowingModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.UserProfileFeedViewModel
import com.squareup.picasso.Picasso
import java.util.Date

private const val TAG = "ViewUserProfileFragment"

class ViewUserProfileFragment(
    private val profileOwnerId: String,
    private val viewerId: String,
    private val viewModel: UserProfileFeedViewModel
) : Fragment(), View.OnClickListener {
    private var _binding: FragmentViewUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbTools: DBTools
    private lateinit var postAdapter: PostAdapter
    private lateinit var dialogueTools: DialogueTools


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() Called")
        _binding = FragmentViewUserProfileBinding.inflate(inflater, container, false)
        dbTools = DBTools()
        dialogueTools = DialogueTools(requireContext())
        return binding.root
    }

    /**
     * Sets up the bindings for the user profile
     *
     */
    private fun setupBindings() {
        // gets the user information and binds it the user information to the layout
        dbTools.getUser(profileOwnerId) { user ->
            if (user != null) {
                bindUserProfile(user)
            } else {
                // display an error and end activity
                Log.w(TAG, "Error loading owner user profile $profileOwnerId")
                dialogueTools.createOKDialogueMessage(
                    "Error Loading Profile",
                    "There was an error loading this user's profile. Please try again!"
                ) {
                    requireActivity().finish()
                }
            }
        }

        // setting up recycler view
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = postAdapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadUserFeed(profileOwnerId, isPagination = true)
                }
            }
        })

        // setting up clickable objects
        binding.actionFollowUser.setOnClickListener(this)
        binding.actionBlockUser.setOnClickListener(this)
        binding.actionUnfollowUser.setOnClickListener(this)
        binding.followingCountTextView.setOnClickListener(this)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        dbTools.checkIfViewerFollowsUser(viewerId, profileOwnerId) { follows ->
            if (follows) {
                binding.actionFollowUser.visibility = View.GONE
                binding.actionUnfollowUser.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        postAdapter = PostAdapter(mutableListOf(), dbTools) { postId ->
            openCommentFragment(postId)
        }
        if (viewerId == profileOwnerId) {
            binding.actionBlockUser.visibility = View.GONE
            binding.actionFollowUser.visibility = View.GONE
        }
        setupBindings()
        setupObserverViewModel()
        loadUserProfile()
    }

    /**
     * Calls viewmodel to load user profile and load user feed
     *
     */
    private fun loadUserProfile() {
        viewModel.loadUserProfile(profileOwnerId) { success ->
            if (!success) {
                // unable to load user profile...have to handle in some way
                Log.w(TAG, "Unable to load user's profile")
                dialogueTools.createOKDialogueMessage(
                    "Profile Load Error",
                    "Unable to load the specific user's data. Please try opening the user's profile again"
                ) {
                    requireActivity().finish()
                }
            } else {
                Log.d(tag, "Successfully loaded user profile. Loading user feed now")
                viewModel.loadUserFeed(profileOwnerId, isPagination = false)
            }
        }
    }

    /**
     * Sets up the observer view model by binding the information from the user's
     * profile to their appropriate text / image views and then loads the user's profile
     *
     */
    private fun setupObserverViewModel() {
        Log.d(tag, "setupObserverViewModel called")
        viewModel.userModel.observe(viewLifecycleOwner) { userModel ->
            userModel?.let {
                bindUserProfile(it)
            }
        }

        viewModel.timelinePosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.updatePosts(posts)
        }
    }

    /**
     * Binds the information from the user model to the user profile
     *
     * @param userModel the model of the profile owner
     */
    private fun bindUserProfile(userModel: UserModel) {
        binding.userNameTextView.text = userModel.name
        binding.userBioTextView.text = userModel.bio
        binding.followersCountTextView.text = String.format(userModel.followerCount.toString())
        binding.followingCountTextView.text = String.format(userModel.followingCount.toString())
        binding.postsCountTextView.text = String.format(userModel.postCount.toString())
        Picasso.get().load(userModel.profilePictureUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_default_profile_picture)
            .into(binding.userProfilePicture)
    }

    private fun handleOnFollowUserClicked() {
        Log.d(TAG, "handleOnFollowUserClicked() called")
        dbTools.getUser(profileOwnerId) { doc ->
            if (doc != null) {
                // need logic for unfollowing still
                val followingModel = FollowingModel(
                    currentUserId = viewerId,
                    followedUserId = profileOwnerId,
                    followedUserProfileUrl = doc.profilePictureUrl,
                    followingStatus = "Following",
                    dateFollowed = Date()
                )
                dbTools.followUser(followingModel) { success ->
                    if (success) {
                        Log.d(TAG, "Successfully followed user")
                        binding.actionFollowUser.visibility = View.GONE
                        binding.actionUnfollowUser.visibility = View.VISIBLE
                        viewModel.loadUserProfile(profileOwnerId) {}
                    } else {
                        Log.w(TAG, "Error following user: $profileOwnerId")
                    }
                }
            } else {
                Log.d(TAG, "unable to load user information for $profileOwnerId")
            }
        }

    }

    private fun handleOnUnfollowUserClicked() {
        Log.d(TAG, "handleOnUnfollowUserClicked() called")
        dbTools.unfollowUser(viewerId, profileOwnerId) { unfollowed ->
            if (unfollowed) {
                Log.d(TAG, "Successfully unfollowed user")
                binding.actionFollowUser.visibility = View.VISIBLE
                binding.actionUnfollowUser.visibility = View.GONE
                viewModel.loadUserProfile(profileOwnerId) {}
            } else {
                Log.w(TAG, "Error unfollowing user")

            }
        }
    }

    private fun handleOnBlockUserClicked() {
        Log.d(TAG, "handleOnBlockUserClicked() called")
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick() called")
        when (v?.id) {
            binding.actionFollowUser.id -> handleOnFollowUserClicked()
            binding.actionBlockUser.id -> handleOnBlockUserClicked()
            binding.actionUnfollowUser.id -> handleOnUnfollowUserClicked()
            binding.followingCountTextView.id -> {
                startActivity(Intent(activity, ViewFollowingActivity::class.java))
            }
        }
    }


    private fun openCommentFragment(postId: String) {
        val commentFragment = CommentFragment().apply {
            arguments = Bundle().apply {
                putString("POST_ID", postId)
            }
        }
        parentFragmentManager.beginTransaction()
            .add(R.id.fragment_container, commentFragment)
            .addToBackStack(null)
            .commit()
    }
}