package com.foodshare.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.foodshare.databinding.ActivitySplashBinding
import com.foodshare.ui.MainActivity
import com.foodshare.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(1500)
            checkAuthState()
        }
    }

    private fun checkAuthState() {
        if (viewModel.isLoggedIn()) {
            // Sync user data with backend to ensure cached data is up-to-date
            syncAndNavigate()
        } else {
            navigateToAuth()
        }
    }

    private fun syncAndNavigate() {
        lifecycleScope.launch {
            viewModel.syncUser().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Show loading indicator if needed
                    }
                    is Resource.Success, is Resource.Error -> {
                        // Navigate regardless of sync result (user is still logged in)
                        navigateToMain()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
