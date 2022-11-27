package com.swat_uzb.weatherapp.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.swat_uzb.weatherapp.R
import javax.inject.Inject

class CustomAlertDialogFragment @Inject constructor() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = DialogInterface.OnClickListener { _, which ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(KEY_RESPONSE to which)
            )
        }
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_dialog_title))
            .setMessage(getString(R.string.alert_dialog_message))
            .setPositiveButton(getString(R.string.menu_settings), listener)
            .setNegativeButton(getString(R.string.alert_dialog_cancel), listener)
            .create()
    }

    companion object {
        const val TAG = "AlertDialogFragment"
        const val REQUEST_KEY = "$TAG:REQUEST_KEY"
        const val KEY_RESPONSE = "RESPONSE"

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Int) -> Unit
        ) {
            manager.setFragmentResultListener(
                REQUEST_KEY,
                lifecycleOwner
            ) { _, result ->
                listener.invoke(result.getInt(KEY_RESPONSE))
            }
        }
    }
}
