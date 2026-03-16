package com.foodshare.util

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.foodshare.BuildConfig
import com.foodshare.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.ic_placeholder) {
    val fullUrl = when {
        url.isNullOrEmpty() -> null
        url.startsWith("http") -> url
        else -> "${BuildConfig.BASE_URL}${url.removePrefix("/")}"
    }

    Glide.with(context)
        .load(fullUrl)
        .placeholder(placeholder)
        .error(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadCircleImage(url: String?, placeholder: Int = R.drawable.ic_profile_placeholder) {
    val fullUrl = when {
        url.isNullOrEmpty() -> null
        url.startsWith("http") -> url
        else -> "${BuildConfig.BASE_URL}${url.removePrefix("/")}"
    }

    Glide.with(context)
        .load(fullUrl)
        .placeholder(placeholder)
        .error(placeholder)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun String.toFormattedDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        this
    }
}

fun String.toRelativeTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return this

        val now = Date()
        val diff = now.time - date.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            days > 7 -> toFormattedDate()
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        this
    }
}

fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
