package com.example.biketrackingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.biketrackingapp.R
import com.example.biketrackingapp.databinding.ActivityMainBinding
import com.example.biketrackingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment: NavHostFragment

    @Inject
    @Named("isDemo")
    lateinit var name:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.runFragment, R.id.statisticsFragment, R.id.settingFragment, R.id.setUpFragment
            )
        )
        binding.tvToolbarTitle.text=name
        navigateToTrackingFragmentIfNeeded(intent)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setOnItemReselectedListener {  }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.settingFragment, R.id.statisticsFragment, R.id.runFragment ->
                    binding.bottomNavigationView.visibility = View.VISIBLE
                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
        binding.bottomNavigationView.setupWithNavController(navController)
    }
    private fun navigateToTrackingFragmentIfNeeded(intent:Intent?){
        if (intent?.action==ACTION_SHOW_TRACKING_FRAGMENT){
            navController.navigate(R.id.action_global_to_tracking_fragment)
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}