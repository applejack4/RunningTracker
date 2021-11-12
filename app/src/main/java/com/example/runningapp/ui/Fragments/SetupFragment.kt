package com.example.runningapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentSetupBinding
import com.example.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject lateinit var sharedPref : SharedPreferences

    private var _binding : FragmentSetupBinding ?= null

    @set:Inject
    var isFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstTime){
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.setupFragment, true).build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment, savedInstanceState, navOptions)
        }

        _binding!!.tvContinue.setOnClickListener{
            val success = saveDataToSharedPreference()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveDataToSharedPreference() : Boolean{
        val name = _binding!!.etName.text!!.trim().toString()
        val weight = _binding!!.etWeight.text!!.trim().toString()

        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit().putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false).apply()

        val toolBatText = "Let's go $name!"
        return true
    }

}