package com.example.runningapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentSettingsBinding
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject lateinit var sharedPreferences: SharedPreferences
    private var _binding : FragmentSettingsBinding ?= null

    private fun loadSharedPreferences(){
        val name : String? = sharedPreferences.getString(KEY_NAME, "")
        val weight : Float = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        _binding!!.etName.setText(name)
        _binding!!.etWeight.setText(weight.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return _binding!!.root
    }

    override fun onResume() {
        super.onResume()

        _binding!!.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharePreferences()
            if(success){
                Snackbar.make(requireView(), "Success", Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(requireView(), "Failed", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun applyChangesToSharePreferences() : Boolean{
        val nameText = _binding!!.etName.text?.trim().toString()
        val weight = _binding!!.etWeight.text?.trim().toString()

        if(nameText.isEmpty() || weight.isEmpty()){
            return false
        }

        sharedPreferences.edit().
        putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weight.toFloat()).apply()
        return true
    }

}