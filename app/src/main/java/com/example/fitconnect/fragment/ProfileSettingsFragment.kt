package com.example.fitconnect.fragment


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitconnect.activity.MainActivity
import com.example.fitconnect.databinding.FragmentProfileSettingsBinding
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth


private const val TAG = "ProfileSettingsFragment"

class ProfileSettingsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentProfileSettingsBinding? = null
    private val binding
        get() = _binding!!
    private var dbTools: DBTools = DBTools()
    private lateinit var dialogueTools: DialogueTools

    private fun makeSnackbar(message: String) {
        val activity = requireActivity()
        Snackbar.make(
            (activity.findViewById(android.R.id.content)),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    /**
     * Updates the user's email address on both Firebase and FireStore
     *
     */
    private fun updateEmail() {
        Log.d(TAG, "updateEmail() called")
        val user = Firebase.auth.currentUser
        // check if the user is authenticated
        if (user == null) {
            Log.w(TAG, "User is not authenticated")
            makeSnackbar("user is not authenticated")
            logOut()
            return
        }
        // verify user email addresses are not the same and not blank
        val oldEmail = binding.editTextOldEmail.text.toString()
        val newEmail = binding.editTextNewEmail.text.toString()

        if (oldEmail.isEmpty() || newEmail.isEmpty()) {
            Log.w(TAG, "Missing emails")
            makeSnackbar("Email fields cannot be left blank")
            return
        } else if (oldEmail == newEmail) {
            Log.w(TAG, "Matching emails")
            makeSnackbar("New email cannot be old email")
            return
        }

        // Verify user authentication before updating
        user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { update ->
            if (update.isSuccessful) {
                Log.d(TAG, "Sent verification e-mail")
                dialogueTools.createOKDialogueMessage(
                    "EmailVerification Required",
                    "A verification Email has been sent to your new email address $newEmail." +
                            " Please verify it to complete the update."
                )
            } else {
                // user re-authentication required
                Log.w(TAG, "Error updating email in Authentication: ${update.exception?.message}")

                if (update.exception is FirebaseAuthRecentLoginRequiredException) {
                    promptReauthentication()
                } else {
                    Log.d(TAG, "Firebase error with authorizing user..")
                    makeSnackbar("Error reaching server")
                }
            }
        }
    }


    /**
     * Prompts the user to reauthenticate before continuing
     */
    private fun promptReauthentication() {
        Log.d(TAG, "promptReauthentication() called")
        dialogueTools.createOKDialogueMessage(
            "Re-Authentication Required",
            "Re-authentication required before changing e-mail"
        ) {
            val reAuthDialogueFragment = ReAuthDialogueFragment()
            reAuthDialogueFragment.setCallback { reAuthTask ->
                if (reAuthTask) {
                    Log.d(
                        TAG,
                        "Successfully re-authorized user. Please try to change e-mail again."
                    )
                } else {
                    Log.w(TAG, "Unsuccessfully reauthorized user")
                    makeSnackbar("Incorrect user credentials. Please enter the correct information")
                }
            }
            reAuthDialogueFragment.show(
                requireActivity().supportFragmentManager,
                "ReAuthDialogFragment"
            )
        }
    }

    /**
     * Clears all activities and returns the user to the main page while logging them out.
     *
     */
    private fun logOut() {
        Log.d(TAG, "Logging user out")
        val activity = requireActivity()
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        FirebaseAuth.getInstance().signOut()
        activity.finish()
    }

    private fun setCallbackForDeletion(
        user: FirebaseUser,
        reAuthDialogueFragment: ReAuthDialogueFragment
    ) {
        reAuthDialogueFragment.setCallback { reAuthTask ->
            if (reAuthTask) {
                dbTools.deleteUser(user.uid) { userDeleted ->
                    // delete user document from database
                    if (userDeleted) {
                        Log.d(TAG, "User account was deleted successfully from the database")
                    } else {
                        Log.d(TAG, "User could not be found in the database")
                    }
                    // delete user from the authentication database
                    user.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d(TAG, "User account has been deleted.")
                            dialogueTools.createOKDialogueMessage(
                                "Success",
                                "Successfully deleted the account"
                            ) {
                                // log the user out after they click okay
                                logOut()
                            }
                        } else {
                            Log.w(TAG, "User account was not deleted: ${deleteTask.exception}")
                        }
                    }
                }
            } else {
                makeSnackbar("Re-Authentication failed. Please try again")
            }
        }
    }

    /**
     * Deletes the user's account in authentication server and database server
     *
     */
    private fun deleteAccount() {
        Log.d(TAG, "deleteAccount() called")
        val activity = requireActivity()
        val user = Firebase.auth.currentUser
        // handling the case if the user is not an authenticated user
        if (user == null) {
            makeSnackbar("User is not authenticated")
            logOut()
            return
        }
        // Reauthorize user and then delete the account information
        val reAuthDialogFragment = ReAuthDialogueFragment()
        setCallbackForDeletion(user, reAuthDialogFragment)
        reAuthDialogFragment.show(activity.supportFragmentManager, "ReAuthDialogFragment")
    }

    /**
     * Displays the delete account confirmation window
     *
     */
    private fun displayDeleteConfirmation() {
        val activity = requireActivity()
        val dialogMessage = AlertDialog.Builder(activity)
        Log.d(TAG, "confirmDeletion() called")
        dialogMessage.setTitle("Confirm")
            .setMessage("Are you sure you want to delete this account?")
            .setPositiveButton("Delete Account") { dialog, _ ->
                dialog.dismiss() // close message
                deleteAccount() // delete user account
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create().show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstance: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = Firebase.auth.currentUser
        if (user == null) {
            makeSnackbar("Login required")
            logOut()
            return
        }
        Log.d(TAG, "onViewCreated() called")
        dialogueTools = DialogueTools(requireActivity())
        binding.settingsBackButton.setOnClickListener(this)
        binding.btnEditEmail.setOnClickListener(this)
        binding.logoutBtn.setOnClickListener(this)
        binding.btnDeleteAccount.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView() called")
    }


    override fun onClick(v: View) {
        when (v.id) {
            binding.btnEditEmail.id -> {
                Log.d(TAG, "btnEditEmail::success")
                updateEmail()
            }

            binding.settingsBackButton.id -> {
                Log.d(TAG, "settingsBackButton::success")
                requireActivity().finish()
            }

            binding.logoutBtn.id -> {
                Log.d(TAG, "logoutBtn::success")
                _binding = null
                logOut()
            }

            binding.btnDeleteAccount.id -> {
                Log.d(TAG, "btnDeleteAccount::success")
                displayDeleteConfirmation()
            }
        }
    }
}