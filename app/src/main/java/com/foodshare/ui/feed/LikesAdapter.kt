package com.foodshare.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.data.model.LikeUser
import com.foodshare.util.getFullImageUrl

class LikesAdapter(
    private val users: List<LikeUser>
) : RecyclerView.Adapter<LikesAdapter.LikeUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_like_user, parent, false)
        return LikeUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikeUserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class LikeUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfileImage: ImageView = itemView.findViewById(R.id.ivProfileImage)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)

        fun bind(user: LikeUser) {
            tvDisplayName.text = user.displayName

            user.profileImage?.let { profileImage ->
                val fullUrl = getFullImageUrl(profileImage)
                Glide.with(itemView.context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(ivProfileImage)
            } ?: run {
                ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }
    }
}
