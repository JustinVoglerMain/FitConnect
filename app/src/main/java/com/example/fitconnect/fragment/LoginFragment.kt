package com.example.fitconnect.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.activity.TimelineActivity
import com.example.fitconnect.databinding.FragmentLoginBinding
import com.example.fitconnect.ui.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import java.security.NoSuchAlgorithmException

private const val TAG = "LoginFragment"
private const val TAG_OBSERVER = "LoginObserver"

class LoginFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth

    /**
     * Validates the user info. Modified form of what jarek wrote initially to fit
     * the View.OnClickListener
     *
     */
    private fun validateUserInfo() {
        Log.d(TAG, "validateUserInfo() called")
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        try {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.signIn(email, password)
            } else {
                createLoginErrorDialog()
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "validateUserInfo::Exception", e)
            e.printStackTrace()
        }
    }

    /**
     * Supplies the user with a text box notifying them that logging in was unsuccessful
     *
     */
    private fun createLoginErrorDialog() {
        Log.d(TAG, "createLoginErrorDialog() called")
        val manager: FragmentManager = parentFragmentManager
        val fragment = LoginVerificationErrorDialogFragment()
        fragment.show(manager, "login_verification_error")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstance: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        auth = FirebaseAuth.getInstance()
        binding.fragmentLoginButton.setOnClickListener(this)
        binding.fragmentBackButton.setOnClickListener(this)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        // Observe LiveData from ViewModel
        loginViewModel.loginStatus.observe(viewLifecycleOwner) { loggedIn ->
            Log.d(TAG_OBSERVER, "Observer triggered.")
            if (loggedIn) {
                val activity = requireActivity()
                Log.d(TAG_OBSERVER, "Login success. Navigating to MainActivity.")
                val intent = Intent(activity, TimelineActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity.finish()
            } else {
                Log.d(TAG_OBSERVER, "Login failed. Showing error dialog.")
                createLoginErrorDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView() called")
    }


    override fun onClick(v: View) {
        val activity = requireActivity()
        when (v.id) {
            binding.fragmentLoginButton.id -> {
                Log.d(TAG, "loginButton::Success")
                validateUserInfo()

            }

            binding.fragmentBackButton.id -> {
                Log.d(TAG, "backButton::Success")
                activity.finish()
            }
        }
    }
}