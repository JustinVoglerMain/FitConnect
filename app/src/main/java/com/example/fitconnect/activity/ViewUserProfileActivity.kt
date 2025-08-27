package com.example.fitconnect.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.ViewUserProfileFragment
import com.example.fitconnect.ui.UserProfileFeedViewModel

/**
 * The activity representing when a user views someone's profile that is not their own
 *
 *
 * @property profileOwnerId the id of the user being viewed
 * @property userId the id of the user viewing the profile
 */
class ViewUserProfileActivity : BaseActivityFragment<FragmentContainerBinding>() {
    private val arg1 = "PROFILE_OWNER_ID"
    private val arg2 = "USER_ID"
    private lateinit var profileOwnerId: String
    private lateinit var userId: String
    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "ViewUserProfileActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container
    private lateinit var viewModel: UserProfileFeedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // need to set view model before super is called or instantiation error happens
        viewModel = ViewModelProvider(this)[UserProfileFeedViewModel::class.java]
        profileOwnerId = intent.getStringExtra(arg1) ?: "Missing Extras labeled $arg1"
        userId = intent.getStringExtra(arg2) ?: "Missing Extra labeled $arg2"
        super.preventNavBar()
        super.onCreate(savedInstanceState)
        binding.bottomNavigation.visibility = View.GONE
        Log.d(tag, "onCreate() called")
    }


    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy() called")
    }

    override fun getInitialFragment(): Fragment {
        return ViewUserProfileFragment(profileOwnerId, userId, getViewModel())
    }

    private fun getViewModel(): UserProfileFeedViewModel {
        return viewModel
    }
}