package com.example.biketrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.biketrackingapp.R
import com.example.biketrackingapp.databinding.FragmentSettingsBinding
import com.example.biketrackingapp.other.Constants.KET_WEIGHT
import com.example.biketrackingapp.other.Constants.KEY_NAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class SettingFragment :Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        getValuesFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            if (setSettingChanges()){
                Snackbar.make(view,"Setting Changed",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please Enter All Fields",Snackbar.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun getValuesFromSharedPref(){
        binding.etName.setText(sharedPreferences.getString(KEY_NAME,""))
        binding.etWeight.setText(sharedPreferences.getFloat(KET_WEIGHT,80f).toString())

    }
    private fun setSettingChanges():Boolean{
        val textName=binding.etName.text.toString()
        val textWeight=binding.etWeight.text.toString()
        if (textName.isEmpty()||textWeight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME,textName)
            .putFloat(KET_WEIGHT,textWeight.toFloat())
            .apply()
        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}