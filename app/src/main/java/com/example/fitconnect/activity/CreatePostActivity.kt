package com.example.fitconnect.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.CreatePostFragment

class CreatePostActivity : BaseActivityFragment<FragmentContainerBinding>() {
    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "CreatePostActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return CreatePostFragment()
    }
}