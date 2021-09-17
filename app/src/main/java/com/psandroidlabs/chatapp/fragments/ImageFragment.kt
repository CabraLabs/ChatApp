package com.psandroidlabs.chatapp.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentImageBinding


class ImageFragment(private val bitmap: Bitmap?) : DialogFragment() {

    private lateinit var binding: FragmentImageBinding

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (bitmap != null) {
                expandedImage.setImageBitmap(bitmap)
            } else {
                expandedImage.setImageResource(R.mipmap.ic_image_error)
            }

            btnImageFragment.setOnClickListener {
                dismiss()
            }
        }
    }
}