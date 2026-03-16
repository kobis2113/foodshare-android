package com.foodshare.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.util.Resource
import com.foodshare.util.loadCircleImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var ivBackButton: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnChangePhoto: FloatingActionButton
    private lateinit var etDisplayName: TextInputEditText
    private lateinit var btnSave: com.google.android.material.button.MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var cameraPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraPhotoUri?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        viewModel.loadProfile()
    }

    private fun initViews(view: View) {
        ivBackButton = view.findViewById(R.id.ivBackButton)
        tvTitle = view.findViewById(R.id.tvTitle)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
        etDisplayName = view.findViewById(R.id.etDisplayName)
        btnSave = view.findViewById(R.id.btnSave)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnChangePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        ivProfileImage.setOnClickListener {
            showImagePickerDialog()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}_",
            ".jpg",
            requireContext().cacheDir
        )
        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(ivProfileImage)
    }

    private fun saveProfile() {
        val displayName = etDisplayName.text.toString().trim()

        if (displayName.isEmpty()) {
            etDisplayName.error = "Display name is required"
            return
        }

        viewModel.updateProfile(requireContext(), displayName, selectedImageUri)
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    resource.data?.let { user ->
                        etDisplayName.setText(user.displayName)
                        ivProfileImage.loadCircleImage(user.profileImage)
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
