package com.example.fitconnect.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.CommentFragment
import com.example.fitconnect.fragment.CreatePostFragment
import com.example.fitconnect.fragment.CreateWorkoutFragment
import com.example.fitconnect.fragment.CurrentUserFeedFragment
import com.example.fitconnect.fragment.ProfileMainFragment
import com.example.fitconnect.fragment.ReAuthDialogueFragment
import com.example.fitconnect.fragment.SearchFragment
import com.example.fitconnect.fragment.WorkoutFragment
import com.example.fitconnect.ui.CurrentUserFeedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class TimelineActivity : BaseActivityFragment<FragmentContainerBinding>() {
    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "TimelineActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container

    private lateinit var viewModel: CurrentUserFeedViewModel
    private var lastItemId = R.id.nav_home
    private var lastIndex = 0

    private val navItemOrder = listOf(
        R.id.nav_home,
        R.id.nav_search,
        R.id.nav_profile,
        R.id.nav_workout,
        R.id.nav_create
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[CurrentUserFeedViewModel::class.java]
        super.setTimeline()
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        setupNavBar()
        Log.d(tag, "onCreate() called")
        // adding the current fragment onto the fragment container stack
        if (savedInstanceState == null) {
            val fragment = CurrentUserFeedFragment(viewModel)
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, fragment::class.java.simpleName)
                .commit()
            supportFragmentManager.executePendingTransactions()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart() called")
    }


    /**
     * Sets up the navigation bar and its item select listeners
     *
     */
    override fun setupNavBar() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId
            val newFragment: Fragment =
                when(itemId) {
                    R.id.nav_home -> getInitialFragment()
                    R.id.nav_search -> SearchFragment()
                    R.id.nav_profile -> ProfileMainFragment()
                    R.id.nav_workout -> WorkoutFragment()
                    R.id.nav_create -> CreateWorkoutFragment()
                    else -> return@setOnItemSelectedListener false
            }

            this.replaceFragment(newFragment, itemId)
            lastItemId = itemId
            true
        }
    }

    override fun replaceFragment(fragment: Fragment, itemId: Int) {
        // pop off the comment fragment if it is on the stack
        val fragmentIsComment =
            supportFragmentManager.findFragmentByTag(CommentFragment::class.java.simpleName)
        if (fragmentIsComment != null) {
            popStackAndWait()
        }

        if (itemId == lastItemId) {
            return
        }

        val fragmentTag = fragment::class.java.simpleName
        val nextIndex = navItemOrder.indexOf(itemId)
        val isForwardNav = nextIndex > lastIndex
        val transaction = supportFragmentManager.beginTransaction().apply {
            if (isForwardNav) {
                setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
            } else {
                setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right,
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
                )
            }
        }

        supportFragmentManager.fragments.forEach {
            if (it.tag != CurrentUserFeedFragment::class.java.simpleName) {
                Log.w(tag, "Removing previous fragment ${it.tag}")
                // removing any fragments that aren't the current user feed
                transaction.remove(it)
            } else {
                Log.d(tag, "This should be current user feed ${it.tag}")
                // hide the current user feed if we can't replace the current fragment
                transaction.hide(it)
            }
        }

        val fragmentInStack = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragmentInStack == null) {
            Log.w(tag, "Fragment not in stack $fragmentTag")
            transaction.add(fragmentContainerId, fragment, fragmentTag)
        } else {
            transaction.show(fragmentInStack)
        }
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
        lastIndex = nextIndex
    }

    private fun popStackAndWait() {
        supportFragmentManager.popBackStack()
        supportFragmentManager.executePendingTransactions()
    }



    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume() called")
        val user = Firebase.auth.currentUser
        if (user == null) {
            Log.w(tag, "User session has expired on resume")
            val reAuthDialogueFragment = ReAuthDialogueFragment()
            reAuthDialogueFragment.setCallback { reAuthSuccessful ->
                if (!reAuthSuccessful) {
                    Snackbar.make(
                        (this.findViewById(android.R.id.content)),
                        "Incorrect credentials. Please log in again.",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Firebase.auth.signOut()
                    this.finish()
                }
            }
        }
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
        Log.d(tag, "getInitialFragment() called")
        return CurrentUserFeedFragment(getViewModel())
    }

    private fun getViewModel(): CurrentUserFeedViewModel {
        return viewModel
    }
}