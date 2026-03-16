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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()
    private val args: EditPostFragmentArgs by navArgs()

    private lateinit var ivBackButton: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var ivPostImage: ImageView
    private lateinit var btnChangeImage: MaterialButton
    private lateinit var etMealName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnDelete: MaterialButton
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
        return inflater.inflate(R.layout.fragment_edit_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        viewModel.loadPost(args.postId)
    }

    private fun initViews(view: View) {
        ivBackButton = view.findViewById(R.id.ivBackButton)
        tvTitle = view.findViewById(R.id.tvTitle)
        ivPostImage = view.findViewById(R.id.ivPostImage)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)
        etMealName = view.findViewById(R.id.etMealName)
        etDescription = view.findViewById(R.id.etDescription)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        btnDelete = view.findViewById(R.id.btnDelete)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnChangeImage.setOnClickListener {
            showImagePickerDialog()
        }

        ivPostImage.setOnClickListener {
            showImagePickerDialog()
        }

        btnUpdate.setOnClickListener {
            updatePost()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Image")
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
            .centerCrop()
            .into(ivPostImage)
    }

    private fun updatePost() {
        val mealName = etMealName.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (mealName.isEmpty()) {
            etMealName.error = "Meal name is required"
            return
        }

        viewModel.updatePost(
            requireContext(),
            args.postId,
            selectedImageUri,
            mealName,
            description.ifEmpty { null }
        )
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePost(args.postId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.post.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    resource.data?.let { post ->
                        etMealName.setText(post.mealName)
                        etDescription.setText(post.description ?: "")
                        Glide.with(this)
                            .load(post.image)
                            .centerCrop()
                            .into(ivPostImage)
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.updatePostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnUpdate.isEnabled = false
                    btnDelete.isEnabled = false
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    btnUpdate.isEnabled = true
                    btnDelete.isEnabled = true
                    Toast.makeText(context, "Post updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    btnUpdate.isEnabled = true
                    btnDelete.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deletePostResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnUpdate.isEnabled = false
                    btnDelete.isEnabled = false
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    btnUpdate.isEnabled = true
                    btnDelete.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
