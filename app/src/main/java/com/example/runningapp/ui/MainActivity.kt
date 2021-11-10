package com.example.runningapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivityMainBinding
import com.example.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var framelay : FrameLayout
    private lateinit var _binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)

        navigateToTrackingFragmentIfNeeded(intent)

        framelay = findViewById<FrameLayout>(R.id.navHostFragment)

        setSupportActionBar(_binding.toolbar)
        _binding.bottomNavigationView.setupWithNavController(framelay.findNavController())

        framelay.findNavController().addOnDestinationChangedListener{_, destination, _ ->
            when(destination.id){
                R.id.SettingsFragment, R.id.StatisticsFragment, R.id.runFragment ->
                    _binding.bottomNavigationView.visibility = View.VISIBLE
                else ->
                    _binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent : Intent){
        if(intent.action == ACTION_SHOW_TRACKING_FRAGMENT){
            framelay.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}