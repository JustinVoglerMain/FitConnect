package com.example.fitconnect.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentContainerBinding
import com.example.fitconnect.fragment.ViewFollowingFragment
import com.example.fitconnect.ui.FollowingViewModel

class ViewFollowingActivity : BaseActivityFragment<FragmentContainerBinding>() {
    override val bindingInflater: (LayoutInflater) -> FragmentContainerBinding
        get() = FragmentContainerBinding::inflate
    override val tag: String
        get() = "ViewFollowingActivity"
    override val fragmentContainerId: Int
        get() = R.id.fragment_container
    private lateinit var viewModel: FollowingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[FollowingViewModel::class.java]
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
        return ViewFollowingFragment(getViewModel())
    }

    private fun getViewModel(): FollowingViewModel {
        return viewModel
    }
}