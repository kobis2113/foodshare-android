package com.foodshare.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.data.model.Post
import com.foodshare.util.loadImage

class PostGridAdapter(
    private val onPostClick: (Post) -> Unit,
    private val onEditClick: ((Post) -> Unit)? = null,
    private val showEditButton: Boolean = false
) : ListAdapter<Post, PostGridAdapter.PostGridViewHolder>(PostGridDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostGridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_grid, parent, false)
        return PostGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostGridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        private val tvMealName: TextView = itemView.findViewById(R.id.tvMealName)
        private val tvLikesCount: TextView = itemView.findViewById(R.id.tvLikesCount)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPostClick(getItem(position))
                }
            }

            btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(post: Post) {
            tvMealName.text = post.mealName
            tvLikesCount.text = "${post.likesCount}"

            ivPostImage.loadImage(post.image)

            btnEdit.visibility = if (showEditButton) View.VISIBLE else View.GONE
        }
    }

    class PostGridDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
