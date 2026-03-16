package com.foodshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikedPostsFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postGridAdapter: PostGridAdapter

    private lateinit var ivBackButton: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var rvPosts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liked_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadLikedPosts()
    }

    private fun initViews(view: View) {
        ivBackButton = view.findViewById(R.id.ivBackButton)
        tvTitle = view.findViewById(R.id.tvTitle)
        rvPosts = view.findViewById(R.id.rvPosts)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
    }

    private fun setupRecyclerView() {
        postGridAdapter = PostGridAdapter(
            onPostClick = { post ->
                val action = LikedPostsFragmentDirections
                    .actionLikedPostsFragmentToPostDetailFragment(post.id)
                findNavController().navigate(action)
            },
            showEditButton = false
        )
        rvPosts.apply {
            adapter = postGridAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.likedPosts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    val posts = resource.data ?: emptyList()
                    if (posts.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvPosts.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvPosts.visibility = View.VISIBLE
                        postGridAdapter.submitList(posts)
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLikedPosts()
    }
}
