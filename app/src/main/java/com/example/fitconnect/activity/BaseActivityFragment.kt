package com.example.fitconnect.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.transition.Visibility
import androidx.viewbinding.ViewBinding
import com.example.fitconnect.R
import com.example.fitconnect.fragment.CommentFragment
import com.example.fitconnect.fragment.CreatePostFragment
import com.example.fitconnect.fragment.CreateWorkoutFragment
import com.example.fitconnect.fragment.ProfileMainFragment
import com.example.fitconnect.fragment.SearchFragment
import com.example.fitconnect.fragment.WorkoutFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * An abstract base class for any activity fragment
 *
 * @param FragmentContainer - the specific fragment container for this
 */

abstract class BaseActivityFragment<FragmentContainer : ViewBinding> : AppCompatActivity() {
    // allowing for generic type (viewbinding)
    var _binding: FragmentContainer? = null

    //  used for getting a binding - !! indicates the binding cannot be null
    val binding get() = _binding!!

    // readonly property that is a function that returns the binding inflater
    abstract val bindingInflater: (LayoutInflater) -> FragmentContainer

    // readonly property that represents the class name for logging
    abstract val tag: String

    // readonly property that will represent the fragment container id
    abstract val fragmentContainerId: Int

    private var isTimeline = false
    private var containsNavBar = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        Log.d(tag, "onCreate() Called")

        // create fragment if activity is being created for the first time (not being repeated)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainerId, getInitialFragment())
                .commitNow()
        }

        if (!isTimeline && containsNavBar) {
            setupNavBar()
        }

    }

    /**
     * Tells the base activity that the derived child is the timeline and prevents
     * the NavBar from being setup
     *
     */
    protected fun setTimeline() {
        isTimeline = true
    }

    /**
     * Prevent the navabar from being created in a specific activity
     *
     */
    protected fun preventNavBar() {
        containsNavBar = false
    }

    /**
     * Get the fragment related to this
     *
     * @return the activity fragment related to this
     */
    abstract fun getInitialFragment(): Fragment

    /**
     * Allows for replacing function fragment with a new fragment
     *
     * @param fragment the new fragment to be used
     */
    open fun replaceFragment(fragment: Fragment, itemId: Int = -1) {
        if (supportFragmentManager.findFragmentByTag(CommentFragment::class.java.simpleName) != null) {
            supportFragmentManager.popBackStack()
        }
        val fragmentTag = fragment::class.java.simpleName
        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(fragmentContainerId, fragment, fragmentTag)
            transaction.commit()
        } else {
            // Logging when a user double clicks - to prevent from constantly replacing the same fragment
            Log.d(tag, "Fragment with id ${fragment.id} already in stack")
        }
    }

    /**
     * Sets up the navigation bar and its item select listeners
     *
     */
    open fun setupNavBar() {
        val bottomNavigationBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    this.finish()
                    true
                }
                R.id.nav_create -> {
                    replaceFragment(CreateWorkoutFragment())
                    true
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileMainFragment())
                    true
                }
                R.id.nav_workout -> {
                    replaceFragment(WorkoutFragment())
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroy() {
        Log.d(tag, "onDestroy Called")
        super.onDestroy()
        _binding = null
    }

}