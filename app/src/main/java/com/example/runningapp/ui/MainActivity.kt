package com.example.runningapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = _binding.root
        setContentView(view)

        val framelay = findViewById<FrameLayout>(R.id.navHostFragment)

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
}