package com.example.fitconnect.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.activity.MainActivity
import com.example.fitconnect.databinding.FragmentCreatePostBinding
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.CreatePostViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.Date
import java.util.UUID

private const val TAG = "CreatePostFragment"

/**
 * The fragment for when a user wants to create a new post to their timeline
 *
 */
class CreatePostFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialogueTools: DialogueTools
    private lateinit var dbTools: DBTools
    private var imageUri: Uri? = null
    private lateinit var createPostViewModel: CreatePostViewModel

    // Part of letting the user pick an image from their camera roll / gallery
    private val getImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                binding.postImageView.setImageURI(uri)
                binding.postImageView.visibility = View.VISIBLE
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        dbTools = DBTools()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        val context = requireContext()
        dialogueTools = DialogueTools(context)
        binding.postButton.setOnClickListener(this)
        binding.selectImageButton.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)

        createPostViewModel = ViewModelProvider(this)[CreatePostViewModel::class.java]

        createPostViewModel.postContent.observe(viewLifecycleOwner) { content ->
            binding.postButton.isEnabled = !content.isNullOrEmpty()
        }

        //Adds a listener to the post content for making the post button turn inactive/active
        // when the user does or does not add text
        binding.postContentEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                createPostViewModel.setPostContent(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView() called")
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick() called")
        val activity = requireActivity()
        when (v.id) {
            binding.selectImageButton.id -> {
                selectImage()
            }

            binding.postButton.id -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.postButton.isEnabled = false
                createPost()

            }

            binding.backButton.id -> {
                activity.finish()
            }
        }
    }

    /**
     * Allows the user to add images from their camera roll
     */
    private fun selectImage() {
        Log.d(TAG, "selectImage() called")
        getImage.launch("image/*")
    }

    /**
     * Handles creating a user post
     *
     */
    private fun createPost() {
        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "createPost() called")
        val activity = requireActivity()
        val postContent = binding.postContentEditText.text.toString()
        if (postContent.isEmpty() && imageUri == null) {
            Log.d(TAG, "Observer error allowing empty post")
            Snackbar.make(
                (activity.findViewById(android.R.id.content)),
                "Unable to create empty post. " +
                        "Please attach an image or write text and try again.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val user = Firebase.auth.currentUser
        if (user == null) {
            // user not authenticated for some reason
            dialogueTools.createOKDialogueMessage(
                "Authorization error",
                "There was an error authorizing your request. Please log in and try again."
            )
            Log.d(TAG, "Logging user out")
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            FirebaseAuth.getInstance().signOut()
            activity.finish()
            return
        }

        dbTools.getUser(user.uid) { dbUser ->
            if (dbUser == null) {
                return@getUser
            }
            val postId = UUID.randomUUID().toString()
            if (imageUri != null) {
                dbTools.uploadImage(requireContext(), imageUri!!) { imgUrl, deleteHash ->
                    Handler(Looper.getMainLooper()).post {
                        if (imgUrl != null && deleteHash != null) {
                            savePost(dbUser, postId, postContent, imgUrl, deleteHash)
                        } else {
                            dialogueTools.createOKDialogueMessage(
                                "Upload Error",
                                "There was an error uploading your image. Please wait and try again"
                            ) {
                                binding.progressBar.visibility = View.GONE
                                binding.postButton.isEnabled = true
                            }
                        }
                    }
                }
            } else {
                savePost(dbUser, postId, postContent)
            }
        }
    }

    /**
     * Saves the user post to the database
     *
     * @param user the current user's information represented as user model
     * @param postId the unique id representing the post id
     * @param postContent the content for the post
     * @param imageUrl the url of the attached image Optional::Default = null
     * @param imageHash the hash code related to deleting the image Optional::Default = null
     */
    private fun savePost(
        user: UserModel, postId: String, postContent: String,
        imageUrl: String? = null, imageHash: String? = null
    ) {
        val activity = requireActivity()
        val newPost = PostModel().apply {
            this.postId = postId
            this.posterName = user.name
            this.posterUID = user.id
            this.date = Date()
            this.content = postContent
            this.imgURL = imageUrl ?: ""
            this.deleteHash = imageHash ?: ""
            this.isCurrentUser = true
        }
        dbTools.addPost(newPost) { postSuccess ->
            if (!postSuccess) {
                Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    "Error creating post. Please wait a moment and try again.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                dbTools.addPostToUserTimeline(user.id, newPost) { timelineSuccess ->
                    if (!timelineSuccess) {
                        Snackbar.make(
                            activity.findViewById(android.R.id.content),
                            "Error adding post to user timeline", Snackbar.LENGTH_LONG
                        ).show()
                        dbTools.removePost(newPost) {}
                        binding.progressBar.visibility = View.GONE
                        binding.postButton.isEnabled = true
                    } else {
                        Snackbar.make(
                            activity.findViewById(android.R.id.content),
                            "Successfully created post", Snackbar.LENGTH_LONG
                        ).show()
                        binding.progressBar.visibility = View.GONE
                        requireActivity().finish()
                    }
                }
            }
        }
    }
}
