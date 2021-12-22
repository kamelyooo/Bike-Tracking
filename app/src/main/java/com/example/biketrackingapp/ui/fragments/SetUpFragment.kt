package com.example.biketrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.biketrackingapp.R
import com.example.biketrackingapp.databinding.FragmentSetupBinding
import com.example.biketrackingapp.other.Constants.KET_WEIGHT
import com.example.biketrackingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.biketrackingapp.other.Constants.KEY_NAME
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SetUpFragment :Fragment(R.layout.fragment_setup) {
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var sharedPref:SharedPreferences
    
    @JvmField // expose a field
    @field:[Inject Named("isDDemo")] // leave your annotatios unchanged
    var isFirstTimeOpe: Boolean = false
//    @set:Inject
//     var isFirstTimeOpe=true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)

        if (!isFirstTimeOpe){
            val navOption=NavOptions.Builder().setPopUpTo(R.id.setUpFragment,true).build()
            findNavController().navigate(R.id.action_setUpFragment_to_runFragment,savedInstanceState,navOption)
        }
        binding.tvContinue.setOnClickListener {
            if (writePersonalDataToSharedPref()){
                val navOption=NavOptions.Builder().setPopUpTo(R.id.setUpFragment,true).build()
                findNavController().navigate(R.id.action_setUpFragment_to_runFragment,savedInstanceState,navOption)
//                findNavController().navigate(R.id.action_setUpFragment_to_runFragment)
            }else{
                Snackbar.make(requireView()
                ,"Please Enter All Failed"
                ,Snackbar.LENGTH_LONG).show()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun writePersonalDataToSharedPref():Boolean{
        val name=binding.etName.text.toString()
        val weight=binding.etWeight.text.toString()
        if (name.isEmpty()||weight.isEmpty()){
          return  false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KET_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()


        return true
    }
}