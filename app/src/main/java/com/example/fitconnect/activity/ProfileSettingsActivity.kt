package com.example.fitconnect.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.ProfileSettingsFragment
import com.example.fitconnect.fragment.ReAuthDialogueFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class ProfileSettingsActivity : BaseActivityFragment<FragmentContainerBinding>() {

    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "ProfileSettingsActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate() called")
        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart() called")
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
        return ProfileSettingsFragment()
    }
}