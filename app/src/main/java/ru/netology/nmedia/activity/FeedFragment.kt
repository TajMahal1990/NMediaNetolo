package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()

    private val viewModelAuth: AuthViewModel by viewModels()

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val bundle = Bundle()

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                bundle.putString("content", post.content)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment, bundle)
            }

            override fun onLike(post: Post) {
                if (viewModelAuth.authorized) {
                    if (post.likedByMe)
                        viewModel.unlikeById(post.id)
                    else
                        viewModel.likeById(post.id)
                } else
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onImage(image: String) {
                bundle.apply {
                    putString("image", image)
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment, bundle
                )
            }
        })

        binding.container.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.swipeRefresh.isRefreshing = state.refreshing

            if (state.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_loading,
                    BaseTransientBottomBar.LENGTH_LONG
                ).setAction(R.string.retry_loading) {
                    when (state.retryType) {
                        RetryTypes.LIKE -> viewModel.likeById(state.retryId)
                        RetryTypes.UNLIKE -> viewModel.unlikeById(state.retryId)
                        RetryTypes.SAVE -> viewModel.retrySave(state.retryPost)
                        RetryTypes.REMOVE -> viewModel.removeById(state.retryId)
                        else -> viewModel.loadPosts()
                    }
                }
                    .show()
            }
        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            if (it > 0) {
//                binding.newPosts.text = getString(R.string.new_posts)
//                binding.newPosts.visibility = View.VISIBLE
//            }
//            println("Newer count: $it")
//        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing =
//                    it.prepend is LoadState.Loading ||
//                            it.append is LoadState.Loading ||
                    it.refresh is LoadState.Loading
            }
        }

        viewModelAuth.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        binding.newPosts.setOnClickListener {
            viewModel.loadNewPosts()
            binding.container.smoothScrollToPosition(0)
            binding.newPosts.visibility = View.GONE

        }

//        binding.retryButton.setOnClickListener {
//            viewModel.loadPosts()
//        }

        binding.addPost.setOnClickListener {
            if (viewModelAuth.authorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else findNavController().navigate(R.id.action_feedFragment_to_signUpFragment)
        }

        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_dark
        )

        binding.swipeRefresh.setOnRefreshListener(
//            viewModel.loadPosts()
            adapter::refresh
//            binding.newPosts.visibility = View.GONE
        )

        lifecycleScope.launch {
            val scrollingTop = adapter
                .loadStateFlow
                .distinctUntilChangedBy {
                    it.source.refresh
                }
                .map {
                    it.source.refresh is LoadState.NotLoading
                }

            scrollingTop.collectLatest { scrolling ->
                if (scrolling) binding.container.scrollToPosition(0)
            }
        }
        return binding.root
    }
}