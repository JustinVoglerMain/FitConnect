package com.example.fitconnect.tools

import android.app.AlertDialog
import android.content.Context

class DialogueTools(private val context: Context) {

    /**
     * Creates a dialogue box message where in it waits for the user to read and click before
     * dismissing itself
     *
     * @param title the title of the message
     * @param message the message to include
     * @param onOkClicked an action to be taken after the user clicks ok {optional}
     */
    fun createOKDialogueMessage(title: String, message: String, onOkClicked: (() -> Unit)? = null) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok")
            { dialog,
              _ ->
                dialog.dismiss()
                onOkClicked?.invoke()
            }
            .create().show()
    }


}