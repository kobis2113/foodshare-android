package com.foodshare.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.data.model.Comment

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfileImage: ImageView = itemView.findViewById(R.id.ivProfileImage)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tvAuthorName)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)

        fun bind(comment: Comment) {
            tvAuthorName.text = comment.author.displayName
            tvContent.text = comment.text
            tvTimestamp.text = formatTimestamp(comment.createdAt)

            comment.author.profileImage?.let { profileImage ->
                Glide.with(itemView.context)
                    .load(profileImage)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(ivProfileImage)
            } ?: run {
                ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val instant = java.time.Instant.parse(timestamp)
                val now = java.time.Instant.now()
                val duration = java.time.Duration.between(instant, now)

                when {
                    duration.toMinutes() < 1 -> "Just now"
                    duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
                    duration.toHours() < 24 -> "${duration.toHours()}h"
                    duration.toDays() < 7 -> "${duration.toDays()}d"
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

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
