package com.example.fitconnect.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.adapter.PostAdapter
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.UserProfileFeedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

private const val TAG = "ProfileMainFragment"

class ProfileMainFragment : Fragment() {
    private var profileImageView: ImageView? = null
    private var userNameTextView: TextView? = null
    private var userNameEditText: EditText? = null
    private var editNameButton: ImageButton? = null
    private var isEditingName = false
    private var userBioTextView: TextView? = null
    private var editBioButton: ImageButton? = null
    private var isEditingBio = false
    private var userBioEditText: EditText? = null
    //merge with aren i think he has counting on?
    private var followingCountTextView: TextView? = null
    private var followersCountTextView: TextView? = null
    private var postsCountTextView: TextView? = null
    private var userPostsRecyclerView: RecyclerView? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var profilePostAdapter: PostAdapter
    private lateinit var userProfileFeedViewModel: UserProfileFeedViewModel
    private lateinit var user: FirebaseUser
    private var dbTools:DBTools = DBTools()
    private lateinit var dialogueTools: DialogueTools


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragement_profile_main, container, false)
        userProfileFeedViewModel = ViewModelProvider(this)[UserProfileFeedViewModel::class.java]
        profilePostAdapter = PostAdapter(mutableListOf(), dbTools) {postId ->
            openCommentFragment(postId)
        }
        user = FirebaseAuth.getInstance().currentUser!!

        dialogueTools = DialogueTools(this.requireContext())

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userNameEditText = view.findViewById(R.id.userNameEditText)
        editNameButton = view.findViewById(R.id.editNameButton)

        userBioTextView = view.findViewById(R.id.userBioTextView)
        userBioEditText = view.findViewById(R.id.userBioEditText)
        editBioButton = view.findViewById(R.id.editBioButton)
        followingCountTextView = view.findViewById(R.id.followingCountTextView)
        followersCountTextView = view.findViewById(R.id.followersCountTextView)
        postsCountTextView = view.findViewById(R.id.postsCountTextView)
        userPostsRecyclerView = view.findViewById(R.id.userPostsRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        // Example of setting default data - replace with data fetched from a server or database
        setupDefaultProfileData()

        userProfileFeedViewModel.timelinePosts.observe(viewLifecycleOwner) { posts ->
            profilePostAdapter.updatePosts(posts)
        }

//TODO: NEED TO EDIT PHOTO FOR PROFILE

        editBioButton?.setOnClickListener {
            if (isEditingBio) {
                saveBio()
            } else {
                enableBioEditing()
            }
        }

        editNameButton?.setOnClickListener {
            if (isEditingName) {
                saveName()
            } else {
                enableNameEditing()
            }
        }

        loadUserPosts()
    }

    private fun setupRecyclerView() {
        userPostsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        userPostsRecyclerView?.adapter = profilePostAdapter
        userPostsRecyclerView?.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    userProfileFeedViewModel.loadUserFeed(user.uid, isPagination = true)
                }
            }
        })
    }

    private fun enableNameEditing() {
        isEditingName = true
        isEditingBio = false
        userNameTextView?.visibility = View.GONE
        userNameEditText?.visibility = View.VISIBLE
        userNameEditText?.setText(userNameTextView?.text)
        userBioTextView?.visibility = View.GONE
        editBioButton?.visibility = View.GONE
        editNameButton?.setImageResource(R.drawable.ic_save)
    }

    private fun saveName() {
        val newName = userNameEditText?.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                userNameTextView?.text = newName
                userNameTextView?.visibility = View.VISIBLE
                userNameEditText?.visibility = View.GONE
                userBioTextView?.visibility = View.VISIBLE
                editBioButton?.visibility = View.VISIBLE
                editNameButton?.setImageResource(R.drawable.ic_edit)
                isEditingName = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving name", e)
                Snackbar.make(view ?: return@addOnFailureListener, "Failed to save name", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun enableBioEditing() {
        isEditingBio = true
        isEditingName = false
        userBioTextView?.visibility = View.GONE
        userBioEditText?.visibility = View.VISIBLE
        userBioEditText?.setText(userBioTextView?.text)

        userNameTextView?.visibility = View.GONE
        editNameButton?.visibility = View.GONE

        editBioButton?.setImageResource(R.drawable.ic_save) // Change icon to save
    }

    private fun saveBio() {
        val newBio = userBioEditText?.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("bio", newBio)
            .addOnSuccessListener {
                // Update UI on successful bio save
                userBioTextView?.text = newBio
                userBioTextView?.visibility = View.VISIBLE
                userBioEditText?.visibility = View.GONE

                userNameTextView?.visibility = View.VISIBLE
                editNameButton?.visibility = View.VISIBLE

                editBioButton?.setImageResource(R.drawable.ic_edit) // Change icon back to edit
                isEditingBio = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving bio", e)
                // Optionally show an error message to the user
            }
    }

    private fun openCommentFragment(postId: String) {
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


    private fun loadUserPosts() {
        userProfileFeedViewModel.loadUserFeed(user.uid, isPagination = true)
    }

    private fun setupDefaultProfileData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Access the "users" collection and retrieve the document for the logged-in user
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Parse the user data and display it
                        val userName = document.getString("name") ?: "User"
                        val userBio = document.getString("bio") ?: "Hello, this is my bio"
                        val profileImageUrl = document.getString("profileImageUrl")


                        //We need some way to track followers and following count and post count so we can pass it into profile page. Dynamically? Aren may have functionality already
                        //val followingCount =
                        //val followersCount =
                        //val postsCount =

                        userNameTextView?.text = userName
                        userBioTextView?.text = userBio

                        // Load profile image if URL is available
                        profileImageUrl?.let {
                            Picasso.get().load(it).into(profileImageView)
                        }
                    } else {
                        // Handle the case where the user document doesn't exist
                        userNameTextView?.text = getString(R.string.no_name)
                        userBioTextView?.text = getString(R.string.no_bio)
                        Log.d(TAG, "No name and no bio found")
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "loading user info failed")
                    // Handle the error (e.g., display a message to the user)
                    //maybe force logout?
                }
        } else {
            // User is not logged in; handle accordingly
            Log.d(TAG, "user may not be logged in")
            //force logout?
        }
    }



}