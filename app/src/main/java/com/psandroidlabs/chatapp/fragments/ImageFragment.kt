package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentImageBinding
import com.psandroidlabs.chatapp.utils.PictureManager

class ImageFragment : Fragment(R.layout.fragment_image) {

    private lateinit var binding: FragmentImageBinding

    private val args: ImageFragmentArgs by navArgs()

    private val navController: NavController by lazy {
        findNavController()
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

            if (args.path != null) {
                val path = args.path
                expandedImage.setImageBitmap(path?.let { PictureManager.fileToBitmap(it) })
            }

            btnImageFragment.setOnClickListener {
                navController.popBackStack()
            }
        }
    }
}