package com.foodshare.ui.post

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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()

    private lateinit var ivBackButton: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var ivPostImage: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var etMealName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnNutritionTips: MaterialButton
    private lateinit var tvNutritionTips: TextView
    private lateinit var btnCreate: MaterialButton
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
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        ivBackButton = view.findViewById(R.id.ivBackButton)
        tvTitle = view.findViewById(R.id.tvTitle)
        ivPostImage = view.findViewById(R.id.ivPostImage)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        etMealName = view.findViewById(R.id.etMealName)
        etDescription = view.findViewById(R.id.etDescription)
        btnNutritionTips = view.findViewById(R.id.btnNutritionTips)
        tvNutritionTips = view.findViewById(R.id.tvNutritionTips)
        btnCreate = view.findViewById(R.id.btnCreate)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSelectImage.setOnClickListener {
            showImagePickerDialog()
        }

        ivPostImage.setOnClickListener {
            showImagePickerDialog()
        }

        btnNutritionTips.setOnClickListener {
            val mealName = etMealName.text.toString().trim()
            if (mealName.isNotEmpty()) {
                viewModel.getNutritionTips(mealName)
            } else {
                Toast.makeText(context, "Enter a meal name first", Toast.LENGTH_SHORT).show()
            }
        }

        btnCreate.setOnClickListener {
            createPost()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Image")
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
        ivPostImage.visibility = View.VISIBLE
        btnSelectImage.visibility = View.GONE
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(ivPostImage)
    }

    private fun createPost() {
        val mealName = etMealName.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (mealName.isEmpty()) {
            etMealName.error = "Meal name is required"
            return
        }

        viewModel.createPost(
            requireContext(),
            selectedImageUri!!,
            mealName,
            description.ifEmpty { null }
        )
    }

    private fun observeViewModel() {
        viewModel.createPostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnCreate.isEnabled = false
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    btnCreate.isEnabled = true
                    Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    btnCreate.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.nutritionTips.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    btnNutritionTips.isEnabled = false
                    tvNutritionTips.text = "Loading tips..."
                    tvNutritionTips.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    btnNutritionTips.isEnabled = true
                    resource.data?.let { tips ->
                        tvNutritionTips.text = tips
                        tvNutritionTips.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    btnNutritionTips.isEnabled = true
                    tvNutritionTips.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
