package com.example.fitconnect.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.fitconnect.databinding.DialogueReauthBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "ReAuthDialogueFragment"

class ReAuthDialogueFragment : DialogFragment() {
    private lateinit var callback: (Boolean) -> Unit
    private var _binding: DialogueReauthBinding? = null
    private val binding get() = _binding!!

    fun setCallback(callback: (Boolean) -> Unit) {
        this.callback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog() called")
        val builder = AlertDialog.Builder(requireContext())
        _binding = DialogueReauthBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        binding.authenticateButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            reAuthenticateUser(email, password)
        }
        return builder.create()
    }

    private fun reAuthenticateUser(email: String, password: String) {
        Log.d(TAG, "reAuthenticateUser() called")
        val credential = EmailAuthProvider.getCredential(email, password)
        FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { task ->
                callback.invoke(task.isSuccessful)
                dismiss()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        _binding = null
    }
}