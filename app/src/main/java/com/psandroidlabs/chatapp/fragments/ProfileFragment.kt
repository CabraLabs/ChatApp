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

    private var userPhoto: Bitmap? = null
    private lateinit var imageUri: Uri
    private lateinit var imageName: String

    private val navController: NavController by lazy {
        findNavController()
    }

    private val registerTakePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            val square = PictureManager.uriToBitmap(imageUri, requireContext().contentResolver).toSquare()
            if (square != null) {
                userPhoto = square
            }
            binding.avatar.setImageBitmap(userPhoto)
        }
    }

    private val registerChoosePhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val square = PictureManager.uriToBitmap(uri, requireContext().contentResolver).toSquare()
            imageName = PictureManager.setImageName()
            
            val uri = square?.let { PictureManager.bitmapToFile(it, imageName) }

            if (square != null) {
                userPhoto = square
                binding.avatar.setImageBitmap(userPhoto)
            }
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
                        val bitmap = PictureManager.loadMyAvatar()
                        if (bitmap != null) {
                            userPhoto = bitmap
                            avatar.setImageBitmap(PictureManager.loadMyAvatar())
                        }
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
                    val bitmap = userPhoto
                    val extras = FragmentNavigatorExtras(binding.avatar to "image_big")
                    if (bitmap != null) {
                        val uri = PictureManager.bitmapToFile(bitmap, imageName)
                        if(uri.path != null) {
                            val bundle: Bundle = bundleOf("path" to uri.path)
                            findNavController().navigate(
                                R.id.action_profileFragment_to_imageFragment,
                                bundle,
                                null,
                                extras
                            )
                        }
                    } else {
                        findNavController().navigate(
                            R.id.action_profileFragment_to_imageFragment,
                            null,
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
                val bitmap = userPhoto
                if (bitmap != null) {
                    val uri = PictureManager.bitmapToFile(bitmap, imageName)
                    AppPreferences.saveClient(
                        userNameField.text.toString(),
                        clientAvatar = uri.path,
                        context = context
                    )
                    navController.popBackStack()
                } else {
                    AppPreferences.saveClient(
                        userNameField.text.toString(),
                        context = context
                    )
                }

            }
        }
    }

    private fun takePhoto() {
        imageName = PictureManager.setImageName()
        imageUri = PictureManager.createUri(imageName)
        registerTakePhoto.launch(imageUri)
    }

    private fun choosePicture() {
        registerChoosePhoto.launch("image/")
    }
}