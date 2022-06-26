package com.swat_uzb.weatherapp.utils

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.swat_uzb.weatherapp.databinding.CustomDialogFragmentBinding
import javax.inject.Inject

class CustomDialogFragment @Inject constructor() : DialogFragment() {
    private var _binding: CustomDialogFragmentBinding? = null
    private val binding get() = _binding!!
    private val listener = DialogInterface.OnClickListener { _, which ->
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(KEY_RESPONSE to which))

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        setupClickListeners()
    }


    companion object {
        const val TAG = "CustomDialogFragment"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_SUBTITLE = "KEY_SUBTITLE"
        private const val KEY_POSITIVE = "KEY_POSITIVE"
        private const val KEY_NEGATIVE = "KEY_NEGATIVE"
        const val REQUEST_KEY = "$TAG:defaultRequestKEY"
        const val KEY_RESPONSE = "RESPONSE"

        fun show(
            manager: FragmentManager,
            title: String?,
            subTitle: String,
            negative: String,
            positive: String
        ) {
            val dialogFragment = CustomDialogFragment()
            dialogFragment.arguments = bundleOf(
                KEY_TITLE to title,
                KEY_SUBTITLE to subTitle,
                KEY_NEGATIVE to negative,
                KEY_POSITIVE to positive
            )
            dialogFragment.show(manager, TAG)
        }

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

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 32, 0, 32, 64)
            setBackgroundDrawable(inset)

        }
    }

    private fun setup() {
        if (arguments?.getString(KEY_TITLE) == null) {
            binding.customDialogFragmentHeader.apply {
                visibility = View.GONE
            }
        } else {
            binding.customDialogFragmentHeader.apply {
                visibility = View.VISIBLE
                text = arguments?.getString(KEY_TITLE)
            }
        }
        with(binding) {
            with(arguments) {
                this?.let {
                    customDialogFragmentContent.text = getString(KEY_SUBTITLE)
                    customDialogFragmentPositiveButton.text = getString(KEY_POSITIVE)
                    customDialogFragmentNegativeButton.text = getString(KEY_NEGATIVE)
                }
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
           customDialogFragmentPositiveButton.setOnClickListener {
                listener.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                Log.d(TAG, "Positive Button")
                dismiss()
            }

            customDialogFragmentNegativeButton.setOnClickListener {
                listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
                Log.d(TAG, "Negative Button")
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}