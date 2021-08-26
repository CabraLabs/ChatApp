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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentProfileBinding
import com.psandroidlabs.chatapp.utils.AppPreferences
import com.psandroidlabs.chatapp.utils.PictureManager
import com.psandroidlabs.chatapp.utils.toSquare


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
            userPhoto = image.toSquare()!!
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

        initializeButtons()
    }

    private fun initializeButtons() {
        with(binding) {
            context?.let { context ->

                val preference = AppPreferences.getClient(context)

                if (preference.isNotEmpty()) {
                    userNameField.setText(AppPreferences.getClient(context)[0])
                    if (!preference[3].isNullOrBlank()) {
                        val bitmap = preference[2]?.let { PictureManager.fileToBitmap(it) }
                        if (bitmap != null) {
                            avatar.setImageBitmap(bitmap)
                            userPhoto = bitmap
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
                    PictureManager.dialogImage(context, userPhoto)
                }

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

            btnSave.setOnClickListener {
                val uri = userPhoto.let { bitmap -> PictureManager.bitmapToFile(context, bitmap) }
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