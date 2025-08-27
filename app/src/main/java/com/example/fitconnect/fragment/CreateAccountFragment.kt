package com.example.fitconnect.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.databinding.FragmentCreateAccountBinding
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.AccountViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "CreateAccountFragment"

class CreateAccountFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var dialogueTools: DialogueTools
    private lateinit var auth: FirebaseAuth

    /**
     * Creates the authentication account for the user on the firebase
     * account (This is for SECURE connections -- saves user password)
     *
     */
    private fun createAuthenticationAndAccount() {
        val userEmail = binding.etEmail.text.toString()
        val userPassword = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val first = binding.etFirstName.text.toString()
        val last = binding.etLastName.text.toString()
        val name = "$first $last"
        accountViewModel.createAccount(name, userEmail, userPassword, confirmPassword)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        val context = requireContext()
        dialogueTools = DialogueTools(context)
        auth = Firebase.auth
        binding.btnCreateAccount.setOnClickListener(this)
        binding.btnReturnHomepage.setOnClickListener(this)
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        accountViewModel.verificationStatus.observe(viewLifecycleOwner) { status ->
            if (status) {
                val activity = requireActivity()
                Log.d(TAG, "Account creation successful")
                dialogueTools.createOKDialogueMessage(
                    "Success",
                    "Account has been created successfully"
                ) {
                    activity.finish()
                }
            } else {
                Log.d(TAG, "Account creation unsuccessful")
                dialogueTools.createOKDialogueMessage(
                    "Error",
                    "There was an error creating your account." +
                            " Please verify you have entered the correct" +
                            " information and try again."
                )
            }
        }

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
            binding.btnCreateAccount.id -> {
                Log.d(TAG, "btnCreateAccount::Success")
                createAuthenticationAndAccount()
            }

            binding.btnReturnHomepage.id -> {
                Log.d(TAG, "Returning to main activity")
                activity.finish()
            }
        }
    }
}