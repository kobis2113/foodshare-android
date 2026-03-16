package com.foodshare.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.databinding.FragmentFeedBinding
import com.foodshare.util.Resource
import com.foodshare.util.gone
import com.foodshare.util.toast
import com.foodshare.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onPostClick = { post ->
                val action = FeedFragmentDirections.actionFeedFragmentToPostDetailFragment(post.id)
                findNavController().navigate(action)
            },
            onLikeClick = { post, position ->
                viewModel.toggleLike(post.id, position)
            },
            onCommentClick = { post ->
                val action = FeedFragmentDirections.actionFeedFragmentToPostDetailFragment(post.id)
                findNavController().navigate(action)
            }
        )

        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts(refresh = true)
        }
    }

    private fun setupFab() {
        binding.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts.toList())

            if (posts.isEmpty()) {
                binding.tvEmptyState.visible()
            } else {
                binding.tvEmptyState.gone()
            }
        }

        viewModel.postsState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    if (postAdapter.itemCount == 0) {
                        binding.progressBar.visible()
                    }
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    requireContext().toast(result.message ?: getString(R.string.error_generic))
                }
            }
        }

        viewModel.likeState.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Error) {
                requireContext().toast(result.message ?: "Failed to like")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
