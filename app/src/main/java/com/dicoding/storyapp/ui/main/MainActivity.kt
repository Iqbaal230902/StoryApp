package com.dicoding.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.storyapp.R
import com.dicoding.storyapp.adapter.LoadingStateAdapter
import com.dicoding.storyapp.adapter.StoryAdapter
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.ui.ViewModelFactory
import com.dicoding.storyapp.ui.maps.MapsActivity
import com.dicoding.storyapp.ui.story.add.AddStoryActivity
import com.dicoding.storyapp.ui.welcome.WelcomeActivity
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class MainActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupToolbar()

        viewModel.viewModelScope.launch {
            viewModel.getSession().collect { user ->
                token = user.token.toString()
                if (token.isEmpty()) {
                    startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                    finish()
                } else {
                    observeStories(token)
                }
            }
        }

        binding.fabAddStory.setOnClickListener {
            addStory()
        }

        binding.swipeRefresh.setOnRefreshListener {
            observeStories(token)
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter()
        binding.rvItemStory.layoutManager = LinearLayoutManager(this)
        binding.rvItemStory.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )

        // Restore scroll position
        binding.rvItemStory.layoutManager?.scrollToPosition(viewModel.scrollPosition)

        // Save scroll position
        binding.rvItemStory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.scrollPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }
        })
    }

    private fun observeStories(token: String) {
        viewModel.story("Bearer $token").observe(this) { pagingData ->
            lifecycleScope.launch {
                storyAdapter.submitData(pagingData)
                binding.swipeRefresh.isRefreshing = false

                // Restore scroll position after data is submitted
                binding.rvItemStory.layoutManager?.scrollToPosition(viewModel.scrollPosition)
            }
        }
    }

    private fun setupToolbar() {
        binding.ToolbarMain.setOnMenuItemClickListener(this)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun openMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun addStory() {
        val addStory = Intent(this, AddStoryActivity::class.java)
        startActivityForResult(addStory, ADD_STORY_REQUEST_CODE)
    }

    private fun performLogout() {
        viewModel.logout()
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.logut_option -> {
                showLogoutConfirmationDialog()
                true
            }
            R.id.menu_maps -> {
                openMapsActivity()
                true
            }
            else -> false
        }
    }

    companion object {
        private const val ADD_STORY_REQUEST_CODE = 100
    }
}
