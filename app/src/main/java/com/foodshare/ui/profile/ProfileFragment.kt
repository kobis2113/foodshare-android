package com.foodshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.data.model.User
import com.foodshare.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var progressBar: ProgressBar
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPostsCount: TextView
    private lateinit var tvLikesCount: TextView
    private lateinit var btnMyPosts: MaterialButton
    private lateinit var btnLikedPosts: MaterialButton
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        viewModel.loadProfile()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        tvDisplayName = view.findViewById(R.id.tvDisplayName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPostsCount = view.findViewById(R.id.tvPostsCount)
        tvLikesCount = view.findViewById(R.id.tvLikesCount)
        btnMyPosts = view.findViewById(R.id.btnMyPosts)
        btnLikedPosts = view.findViewById(R.id.btnLikedPosts)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnMyPosts.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myPostsFragment)
        }

        btnLikedPosts.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_likedPostsFragment)
        }

        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    resource.data?.let { bindProfile(it) }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.logoutResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    // Navigate to login
                    activity?.finishAffinity()
                    startActivity(
                        android.content.Intent(
                            requireContext(),
                            com.foodshare.ui.auth.AuthActivity::class.java
                        )
                    )
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindProfile(user: User) {
        tvDisplayName.text = user.displayName
        tvEmail.text = user.email
        tvPostsCount.text = (user.postsCount ?: 0).toString()
        tvLikesCount.text = (user.likesReceived ?: 0).toString()

        user.avatar?.let { avatar ->
            Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfileImage)
        } ?: run {
            ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }
}
