package com.foodshare.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodshare.R
import com.foodshare.util.Resource
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentsFragment : Fragment() {

    private val viewModel: CommentsViewModel by viewModels()
    private val args: CommentsFragmentArgs by navArgs()
    private lateinit var commentAdapter: CommentAdapter

    private lateinit var ivBackButton: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var rvComments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etComment: EditText
    private lateinit var btnSend: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadComments(args.postId)
    }

    private fun initViews(view: View) {
        ivBackButton = view.findViewById(R.id.ivBackButton)
        tvTitle = view.findViewById(R.id.tvTitle)
        rvComments = view.findViewById(R.id.rvComments)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        etComment = view.findViewById(R.id.etComment)
        btnSend = view.findViewById(R.id.btnSend)
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSend.setOnClickListener {
            val content = etComment.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.addComment(args.postId, content)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.comments.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    val comments = resource.data ?: emptyList()
                    if (comments.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvComments.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvComments.visibility = View.VISIBLE
                        commentAdapter.submitList(comments)
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.addCommentResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    btnSend.isEnabled = false
                }
                is Resource.Success -> {
                    btnSend.isEnabled = true
                    etComment.text.clear()
                    // Reload comments to show the new one
                    viewModel.loadComments(args.postId)
                }
                is Resource.Error -> {
                    btnSend.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
