package com.example.fitconnect.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Simple class for bringing up a login verification error dialog fragment
 *
 */
class LoginVerificationErrorDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle("login_verification_error")
            .setMessage("Login Verification Failed")
            .setPositiveButton("OK") { _, _ -> }.create()
    }
}