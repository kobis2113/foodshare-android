package com.foodshare.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.foodshare.R
import com.foodshare.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Custom item selection to handle navigation properly
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.feedFragment -> {
                    // Pop back to feed root, or navigate if not in feed stack
                    if (!navController.popBackStack(R.id.feedFragment, false)) {
                        navController.navigate(R.id.feedFragment)
                    }
                    true
                }
                R.id.profileFragment -> {
                    // Pop back to profile root, or navigate if not in profile stack
                    if (!navController.popBackStack(R.id.profileFragment, false)) {
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }

        // Handle re-selecting the same tab - pop back to root
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            // Pop back stack to the root of the current tab
            when (item.itemId) {
                R.id.feedFragment -> {
                    navController.popBackStack(R.id.feedFragment, false)
                }
                R.id.profileFragment -> {
                    navController.popBackStack(R.id.profileFragment, false)
                }
            }
        }

        // Sync bottom nav with current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment, R.id.postDetailFragment, R.id.commentsFragment,
                R.id.addPostFragment, R.id.editPostFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.feedFragment)?.isChecked = true
                }
                R.id.profileFragment, R.id.editProfileFragment,
                R.id.myPostsFragment, R.id.likedPostsFragment -> {
                    binding.bottomNavigation.menu.findItem(R.id.profileFragment)?.isChecked = true
                }
            }
        }
    }
}
