package com.foodshare.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodshare.R
import com.foodshare.data.model.Post
import com.foodshare.data.repository.AuthRepository
import com.foodshare.databinding.FragmentFeedBinding
import com.foodshare.util.Resource
import com.foodshare.util.getFullImageUrl
import com.foodshare.util.gone
import com.foodshare.util.toast
import com.foodshare.util.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    @Inject
    lateinit var authRepository: AuthRepository

    private var currentUserId: String? = null

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

        // Load current user ID asynchronously
        viewLifecycleOwner.lifecycleScope.launch {
            val user = authRepository.getCachedUser().first()
            currentUserId = user?.id
        }

        setupToolbar()
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
            },
            onProfileClick = { post ->
                if (post.author.id == currentUserId) {
                    // Navigate to profile tab
                    findNavController().navigate(R.id.profileFragment)
                } else {
                    // Show user info dialog for other users
                    showUserProfileDialog(post)
                }
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

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_feed)
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
        searchView.setOnCloseListener {
            viewModel.setSearchQuery("")
            false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupFab() {
        binding.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }
    }

    private fun observeViewModel() {
        val updateEmptyState = {
            val posts = viewModel.posts.value.orEmpty()
            postAdapter.submitList(posts.toList())
            if (posts.isEmpty()) {
                binding.tvEmptyState.visible()
                val isSearch = !viewModel.searchQuery.value.isNullOrBlank()
                binding.tvEmptyState.text = if (isSearch) {
                    getString(R.string.no_search_results)
                } else {
                    getString(R.string.no_posts)
                }
            } else {
                binding.tvEmptyState.gone()
            }
        }

        viewModel.posts.observe(viewLifecycleOwner) { updateEmptyState() }
        viewModel.searchQuery.observe(viewLifecycleOwner) { updateEmptyState() }

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

    override fun onResume() {
        super.onResume()
        // Auto-refresh when returning to the feed
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUserProfileDialog(post: Post) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_profile, null)
        dialog.setContentView(dialogView)

        val ivProfileImage = dialogView.findViewById<ImageView>(R.id.ivProfileImage)
        val tvDisplayName = dialogView.findViewById<TextView>(R.id.tvDisplayName)
        val tvPostInfo = dialogView.findViewById<TextView>(R.id.tvPostInfo)

        tvDisplayName.text = post.author.displayName
        tvPostInfo.text = "Author of: ${post.mealName}"

        post.author.profileImage?.let { profileImage ->
            val fullUrl = getFullImageUrl(profileImage)
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfileImage)
        } ?: run {
            ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }

        dialog.show()
    }
}
