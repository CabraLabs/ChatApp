package com.psandroidlabs.chatapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.psandroidlabs.chatapp.databinding.FragmentProfileBinding
import com.psandroidlabs.chatapp.utils.PictureManager

class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private val registerTakePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { image: Bitmap? ->
        binding.avatar.setImageBitmap(image)
    }

    private val registerChoosePhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        binding.avatar.setImageBitmap(
            PictureManager.uriToBitmap(
                uri,
                requireContext().contentResolver
            )
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.forEach {
                when (it.key) {
                    Manifest.permission.CAMERA -> takePhoto()
                    Manifest.permission.READ_EXTERNAL_STORAGE -> choosePicture()
                }
                Log.i(tag, "Permission: ${it.key}, granted: ${it.value}")
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
    }

    private fun initializeButtons(){
        with(binding){
            context?.let { context ->
                btnTakePhoto.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activityResultLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        takePhoto()
                    }
                }

                btnChoosePicture.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activityResultLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    } else {
                        choosePicture()
                    }
                }
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