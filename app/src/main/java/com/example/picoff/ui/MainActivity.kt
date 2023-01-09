package com.example.picoff.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.picoff.MainViewModel
import com.example.picoff.R
import com.example.picoff.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

// TODO Permission handling: ask for camera and storage
// TODO Notificate user when new challenge is coming
// TODO intent to add friend (if intent is detected, navigate to friends screen with name in searchtext)
// TODO detect if offline
// TODO result show for both


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkIfLoggedInWithGoogle()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (viewModel.isFabMenuOpen.value == true)
//                    viewModel.isFabMenuOpen.value = false
//                else
//                    finish()
//            }
//        })

        viewModel.jumpToChallengeList.observe(this) {
            binding.navView.selectedItemId = R.id.navigation_challenges
        }

        viewModel.bottomNavigationVisibility.observe(this, Observer { navVisibility ->
            navVisibility?.let {
                navView.visibility = it
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.initialize()
    }

    private fun checkIfLoggedInWithGoogle() {
        auth = FirebaseAuth.getInstance()
        // If not signed in open SignInActivity
        if (auth.currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }



}
