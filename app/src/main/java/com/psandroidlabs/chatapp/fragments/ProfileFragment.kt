package com.psandroidlabs.chatapp.fragments

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentProfileBinding
import com.psandroidlabs.chatapp.utils.*


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var userPhoto: Bitmap

    private val navController: NavController by lazy {
        findNavController()
    }

    private val registerTakePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { image: Bitmap? ->
        if (image != null) {
            val square = image.toSquare()
            if (square != null) {
                userPhoto = square
            }
            binding.avatar.setImageBitmap(userPhoto)
        }
    }

    private val registerChoosePhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val square = PictureManager.uriToBitmap(uri, requireContext().contentResolver).toSquare()
        if (uri != null && square != null) {
            userPhoto = square
            binding.avatar.setImageBitmap(userPhoto)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeButtons()
    }

    private fun initializeButtons() {
        with(binding) {
            context?.let { context ->

                val preference = AppPreferences.getClient(context)

                if (preference.isNotEmpty()) {
                    userNameField.setText(AppPreferences.getClient(context)[0])
                    if (PictureManager.loadMyAvatar() != null) {
                        avatar.setImageBitmap(PictureManager.loadMyAvatar())
                    } else {
                        avatar.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_goat
                            )
                        )
                    }
                }

                avatar.setOnClickListener {
                    val uri = PictureManager.bitmapToFile(userPhoto)
                    if(uri.path != null) {
                        val bundle: Bundle = bundleOf("path" to uri.path)
                        val extras = FragmentNavigatorExtras(binding.avatar to "image_big")
                        findNavController().navigate(
                            R.id.action_profileFragment_to_imageFragment,
                            bundle,
                            null,
                            extras
                        )
                    }

                }

                btnTakePhoto.setOnClickListener {
                    if(ChatManager.requestPermission(activity, Manifest.permission.CAMERA, Constants.CAMERA_PERMISSION)){
                        takePhoto()
                    }
                }

                btnChoosePicture.setOnClickListener {
                    if(ChatManager.requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, Constants.CHOOSE_IMAGE_GALLERY)){
                        choosePicture()
                    }
                }
            }

            btnSave.setOnClickListener {
                val uri = userPhoto.let { bitmap -> PictureManager.bitmapToFile(bitmap) }
                AppPreferences.saveClient(
                    userNameField.text.toString(),
                    clientAvatar = uri.path,
                    context = context
                )

                navController.popBackStack()
            }
        }
    }

    private fun takePhoto() {
        registerTakePhoto.launch(null)
    }

    private fun choosePicture() {
        registerChoosePhoto.launch("image/")
    }
}