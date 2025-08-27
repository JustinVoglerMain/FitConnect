package com.example.fitconnect.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.adapter.FollowingAdapter
import com.example.fitconnect.databinding.FragmentFollowingBinding
import com.example.fitconnect.model.FollowingModel
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.FollowingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot

private const val TAG: String = "FriendsListFragment"

class ViewFollowingFragment(private val viewModel: FollowingViewModel) : Fragment(),
    View.OnClickListener {
    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialogueTools: DialogueTools
    private lateinit var dbTools: DBTools
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var followingAdapter: FollowingAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "oncCreateView() called")
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        dbTools = DBTools()
        return binding.root
    }

    /**
     * Sets up any bindings required for this fragment
     *
     */
    private fun setBindings() {
        // set up back menu
        binding.toolbar.setTitle("Followers")
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // setup for searching friends list
        binding.searchView.queryHint = "Find Follower"
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchFollowing(query ?: "")
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.searchFollowing(query ?: "")
                return true
            }
        })
    }

    /**
     * Sets up any adapters required for this fragment
     *
     */
    private fun setAdapters() {
        // set up adapter for when user clicks on context menu or friend's profile
        followingAdapter = FollowingAdapter(
            mutableListOf(),
            onFriendClickListener = { friend, document ->
                // handle profile search
                Log.d(TAG, "Friend clicked: ${friend.followedUserName}")
                // @TODO handle viewing friend's profile
            },
            onFriendDeleteClickListener = { friend, document ->
                if (document == null) {
                    Log.d(TAG, "Unable to delete friend as they don't exist")
                    // update this to handle removing friend if this happens
                    return@FollowingAdapter
                }
                showDeleteConfirmationDialogue(friend, document)
            })
    }

    /**
     * Sets up the views of this fragment
     *
     */
    private fun setViews() {
        // setup the recycler view for pagination
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)
        friendsRecyclerView.adapter = followingAdapter
        // setup view model for livedata
        viewModel.friendsList.observe(viewLifecycleOwner) { friends ->
            followingAdapter.updateFollowing(friends)
        }

        viewModel.loadFollowing()
        // Adding scrolling for pagination
        friendsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewModel.isLoading && dy > 0 && viewModel.lastVisibleDocument != null) {
                    val visibleItemCount = friendsRecyclerView.layoutManager?.childCount ?: 0
                    val totalItemCount = friendsRecyclerView.layoutManager?.itemCount ?: 0
                    val pastVisibleItems =
                        (friendsRecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        viewModel.loadFollowing()
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "OnViewCreated() called")
        dialogueTools = DialogueTools(requireContext())
        friendsRecyclerView = binding.friendsRecyclerView
        setBindings()
        setAdapters()
        setViews()
    }

    private fun showDeleteConfirmationDialogue(friend: FollowingModel, document: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Friend")
            .setMessage("Are you sure you want to delete ${friend.followedUserName} from your friends list?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.removeFollowing(document) { deleteResult ->
                    if (deleteResult) {
                        Log.d(TAG, "Successfully removed friend")
                    } else {
                        val user = Firebase.auth.currentUser
                        if (user == null) {
                            // shouldn't happen, but just in case
                            Log.d(TAG, "User is not authenticated")
                            return@removeFollowing
                        }
                        Log.d(
                            TAG,
                            "Error removing ${friend.followedUserId} from ${user.uid}'s friends list"
                        )
                    }
                }
            }
            .setNegativeButton("Cancel") { dialogue, _ ->
                dialogue.dismiss()
            }.show()
    }


    override fun onClick(v: View) {

    }
}