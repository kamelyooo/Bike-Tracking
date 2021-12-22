package com.example.biketrackingapp.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.biketrackingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelAlertDialog :DialogFragment() {
    private var yesListener:(()->Unit)?=null

    fun setYesListener(listener :(()->Unit)){
        yesListener=listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel Run?")
            .setMessage("Are You Sure To Cancel The Run")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("yes"){_,_->
                yesListener?.let {yes->
                   yes()
                }
            }
            .setNegativeButton("No"){DialogInterface,_->
                DialogInterface.cancel()

            }.create()
    }
}