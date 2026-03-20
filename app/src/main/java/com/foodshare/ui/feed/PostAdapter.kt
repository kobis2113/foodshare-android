package com.foodshare.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.data.model.Post
import com.foodshare.databinding.ItemPostBinding
import com.foodshare.util.loadCircleImage
import com.foodshare.util.loadImage
import com.foodshare.util.toRelativeTime

class PostAdapter(
    private val onPostClick: (Post) -> Unit,
    private val onLikeClick: (Post, Int) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onProfileClick: ((Post) -> Unit)? = null
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPostClick(getItem(position))
                }
            }

            binding.btnLike.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLikeClick(getItem(position), position)
                }
            }

            binding.btnComment.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCommentClick(getItem(position))
                }
            }

            // Profile click listeners
            binding.ivProfileImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProfileClick?.invoke(getItem(position))
                }
            }

            binding.tvAuthorName.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProfileClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(post: Post) {
            with(binding) {
                ivProfileImage.loadCircleImage(post.author.profileImage)
                tvAuthorName.text = post.author.displayName
                tvTimestamp.text = post.createdAt.toRelativeTime()
                tvMealName.text = post.mealName
                tvDescription.text = post.description
                ivPostImage.loadImage(post.image)

                tvLikesCount.text = post.likesCount.toString()
                tvCommentsCount.text = post.commentsCount.toString()

                btnLike.setIconResource(
                    if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                btnLike.setIconTintResource(
                    if (post.isLiked) R.color.like_active else R.color.like_inactive
                )
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
