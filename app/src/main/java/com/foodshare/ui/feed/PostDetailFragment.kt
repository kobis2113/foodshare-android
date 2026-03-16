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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.data.model.Post
import com.foodshare.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private val viewModel: PostDetailViewModel by viewModels()
    private val args: PostDetailFragmentArgs by navArgs()

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
    }

    private fun bindPost(post: Post) {
        // Load images
        Glide.with(this)
            .load(post.image)
            .placeholder(R.drawable.ic_placeholder)
            .into(ivPostImage)

        post.author.avatar?.let { avatar ->
            Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfileImage)
        } ?: run {
            ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }

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
            tvCalories.text = "${nutrition.calories ?: 0} kcal"
            tvProtein.text = "${nutrition.protein ?: 0}g"
            tvCarbs.text = "${nutrition.carbs ?: 0}g"
            tvFat.text = "${nutrition.fat ?: 0}g"
            nutrition.tips?.let { tips ->
                tvNutritionTip.visibility = View.VISIBLE
                tvNutritionTip.text = tips
            } ?: run {
                tvNutritionTip.visibility = View.GONE
            }
        } ?: run {
            nutritionCard.visibility = View.GONE
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
}
