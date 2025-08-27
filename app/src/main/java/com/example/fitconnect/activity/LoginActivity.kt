package com.example.fitconnect.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.transition.Visibility
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.LoginFragment
import com.google.firebase.auth.FirebaseAuth

/**
 * Login Activity Fragment
 *
 */

class LoginActivity : BaseActivityFragment<FragmentContainerBinding>() {
    private lateinit var auth: FirebaseAuth
    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "LoginActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.preventNavBar()
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate() called")
        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            Log.d(tag, "User is already logged in.")
            val intent = Intent(this, TimelineActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            this.finish()
            return
        }
        binding.bottomNavigation.visibility = View.GONE

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
        return LoginFragment()
    }
}
