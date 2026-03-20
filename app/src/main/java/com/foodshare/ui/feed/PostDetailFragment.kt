package com.foodshare.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.data.model.LikeUser
import com.foodshare.data.model.Post
import com.foodshare.util.Resource
import com.foodshare.util.loadCircleImage
import com.foodshare.util.loadImage
import com.foodshare.data.repository.AuthRepository
import com.foodshare.util.getFullImageUrl
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private val viewModel: PostDetailViewModel by viewModels()
    private val args: PostDetailFragmentArgs by navArgs()

    @Inject
    lateinit var authRepository: AuthRepository

    private var currentUserId: String? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var ivBackButton: ImageButton
    private lateinit var ivPostImage: ImageView
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvAuthorName: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var tvMealName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnLike: MaterialButton
    private lateinit var tvLikesCount: TextView
    private lateinit var btnComment: MaterialButton
    private lateinit var tvCommentsCount: TextView
    private lateinit var nutritionCard: MaterialCardView
    private lateinit var tvCalories: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvNutritionTip: TextView
    private lateinit var btnGetNutrition: MaterialButton

    private var currentMealName: String? = null
    private var currentPost: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        // Load current user ID
        viewLifecycleOwner.lifecycleScope.launch {
            val user = authRepository.getCachedUser().first()
            currentUserId = user?.id
        }

        viewModel.loadPost(args.postId)
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        contentLayout = view.findViewById(R.id.contentLayout)
        ivBackButton = view.findViewById(R.id.ivBackButton)
        ivPostImage = view.findViewById(R.id.ivPostImage)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        tvAuthorName = view.findViewById(R.id.tvAuthorName)
        tvTimestamp = view.findViewById(R.id.tvTimestamp)
        tvMealName = view.findViewById(R.id.tvMealName)
        tvDescription = view.findViewById(R.id.tvDescription)
        btnLike = view.findViewById(R.id.btnLike)
        tvLikesCount = view.findViewById(R.id.tvLikesCount)
        btnComment = view.findViewById(R.id.btnComment)
        tvCommentsCount = view.findViewById(R.id.tvCommentsCount)
        nutritionCard = view.findViewById(R.id.nutritionCard)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvProtein = view.findViewById(R.id.tvProtein)
        tvCarbs = view.findViewById(R.id.tvCarbs)
        tvFat = view.findViewById(R.id.tvFat)
        tvNutritionTip = view.findViewById(R.id.tvNutritionTip)
        btnGetNutrition = view.findViewById(R.id.btnGetNutrition)
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnLike.setOnClickListener {
            viewModel.toggleLike()
        }

        btnComment.setOnClickListener {
            val action = PostDetailFragmentDirections
                .actionPostDetailFragmentToCommentsFragment(args.postId)
            findNavController().navigate(action)
        }

        btnGetNutrition.setOnClickListener {
            currentMealName?.let { mealName ->
                viewModel.loadNutrition(mealName)
            }
        }

        tvLikesCount.setOnClickListener {
            viewModel.loadLikesUsers()
        }

        // Profile click handlers
        ivProfileImage.setOnClickListener {
            handleProfileClick()
        }

        tvAuthorName.setOnClickListener {
            handleProfileClick()
        }
    }

    private fun handleProfileClick() {
        val post = currentPost ?: return
        if (post.author.id == currentUserId) {
            // Navigate to profile tab
            findNavController().navigate(R.id.profileFragment)
        } else {
            // Show user info dialog
            showUserProfileDialog(post)
        }
    }

    private fun showUserProfileDialog(post: Post) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_profile, null)
        dialog.setContentView(dialogView)

        val ivProfile = dialogView.findViewById<ImageView>(R.id.ivProfileImage)
        val tvName = dialogView.findViewById<TextView>(R.id.tvDisplayName)
        val tvInfo = dialogView.findViewById<TextView>(R.id.tvPostInfo)

        tvName.text = post.author.displayName
        tvInfo.text = "Author of: ${post.mealName}"

        post.author.profileImage?.let { profileImage ->
            val fullUrl = getFullImageUrl(profileImage)
            com.bumptech.glide.Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfile)
        } ?: run {
            ivProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.post.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    contentLayout.visibility = View.GONE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                    resource.data?.let { bindPost(it) }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.likeResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* Loading and Success handled by post update */ }
            }
        }

        viewModel.nutritionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    btnGetNutrition.isEnabled = false
                    btnGetNutrition.text = "Loading..."
                }
                is Resource.Success -> {
                    btnGetNutrition.visibility = View.GONE
                    // Nutrition card will be updated via post observer
                }
                is Resource.Error -> {
                    btnGetNutrition.isEnabled = true
                    btnGetNutrition.text = "Get AI Nutrition Tips"
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.likesUsers.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource) {
                is Resource.Loading -> {
                    // Could show a loading indicator
                }
                is Resource.Success -> {
                    resource.data?.let { users ->
                        showLikesDialog(users)
                        viewModel.clearLikesUsers()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearLikesUsers()
                }
            }
        }
    }

    private fun bindPost(post: Post) {
        // Store post and meal name for later use
        currentPost = post
        currentMealName = post.mealName

        // Load images using extension functions that handle base URL
        ivPostImage.loadImage(post.image)
        ivProfileImage.loadCircleImage(post.author.profileImage)

        // Text content
        tvAuthorName.text = post.author.displayName
        tvTimestamp.text = formatTimestamp(post.createdAt)
        tvMealName.text = post.mealName
        tvDescription.text = post.description ?: ""
        tvLikesCount.text = post.likesCount.toString()
        tvCommentsCount.text = post.commentsCount.toString()

        // Like state
        updateLikeButton(post.isLiked)

        // Nutrition info
        post.nutrition?.let { nutrition ->
            nutritionCard.visibility = View.VISIBLE
            btnGetNutrition.visibility = View.GONE
            tvCalories.text = "${nutrition.calories ?: 0} kcal"
            tvProtein.text = "${String.format("%.1f", nutrition.protein ?: 0.0)}g"
            tvCarbs.text = "${String.format("%.1f", nutrition.carbs ?: 0.0)}g"
            tvFat.text = "${String.format("%.1f", nutrition.fat ?: 0.0)}g"
            if (!nutrition.healthTips.isNullOrEmpty()) {
                tvNutritionTip.visibility = View.VISIBLE
                tvNutritionTip.text = nutrition.healthTips.joinToString("\n• ", "• ")
            } else {
                tvNutritionTip.visibility = View.GONE
            }
        } ?: run {
            nutritionCard.visibility = View.GONE
            btnGetNutrition.visibility = View.VISIBLE
            btnGetNutrition.isEnabled = true
            btnGetNutrition.text = "Get AI Nutrition Tips"
        }
    }

    private fun updateLikeButton(isLiked: Boolean) {
        val iconRes = if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        btnLike.setIconResource(iconRes)
        val tintColor = if (isLiked) R.color.like_active else R.color.like_inactive
        btnLike.setIconTintResource(tintColor)
    }

    private fun formatTimestamp(timestamp: String): String {
        // Simple formatting - in production use proper date parsing
        return try {
            val instant = java.time.Instant.parse(timestamp)
            val now = java.time.Instant.now()
            val duration = java.time.Duration.between(instant, now)

            when {
                duration.toMinutes() < 1 -> "Just now"
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
                duration.toHours() < 24 -> "${duration.toHours()}h ago"
                duration.toDays() < 7 -> "${duration.toDays()}d ago"
                else -> {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
                        .withZone(java.time.ZoneId.systemDefault())
                    formatter.format(instant)
                }
            }
        } catch (e: Exception) {
            timestamp
        }
    }

    private fun showLikesDialog(users: List<LikeUser>) {
        if (users.isEmpty()) {
            Toast.makeText(context, "No likes yet", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_likes, null)
        dialog.setContentView(dialogView)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val rvLikes = dialogView.findViewById<RecyclerView>(R.id.rvLikes)

        tvTitle.text = "Liked by ${users.size} ${if (users.size == 1) "person" else "people"}"

        val adapter = LikesAdapter(users)
        rvLikes.layoutManager = LinearLayoutManager(context)
        rvLikes.adapter = adapter

        dialog.show()
    }
}
